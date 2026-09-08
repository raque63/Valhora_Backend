package com.valhora.backend.cart;

import com.valhora.backend.cart.dto.CartItemResponse;
import com.valhora.backend.cart.dto.CartResponse;
import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.products.Product;
import com.valhora.backend.products.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public CartResponse getCart(UUID userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<UUID> productIds = cartItems.stream().map(CartItem::getProductId).toList();
        Map<UUID, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        List<CartItemResponse> items = cartItems.stream()
                .map(cartItem -> toItemResponse(cartItem, productsById.get(cartItem.getProductId())))
                .filter(Objects::nonNull)
                .toList();

        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, totalItems, subtotal);
    }

    @Transactional
    public CartResponse addOrIncrement(UUID userId, UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> CartItem.builder().userId(userId).productId(productId).quantity(0).build());
        item.setQuantity(item.getQuantity() + 1);
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateQuantity(UUID userId, UUID productId, int quantity) {
        if (quantity <= 0) {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
            return getCart(userId);
        }
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en el carrito"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public CartResponse remove(UUID userId, UUID productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        return getCart(userId);
    }

    @Transactional
    public void clear(UUID userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemResponse toItemResponse(CartItem cartItem, Product product) {
        if (product == null) {
            return null;
        }
        String thumbnailUrl = product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0);
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                thumbnailUrl,
                cartItem.getQuantity(),
                subtotal);
    }
}
