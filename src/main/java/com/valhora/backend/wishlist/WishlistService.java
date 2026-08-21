package com.valhora.backend.wishlist;

import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.products.Product;
import com.valhora.backend.products.ProductMapper;
import com.valhora.backend.products.ProductRepository;
import com.valhora.backend.products.dto.ProductSummaryResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public WishlistService(
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            ProductMapper productMapper) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductSummaryResponse> getWishlist(UUID userId) {
        List<WishlistItem> items = wishlistItemRepository.findByUserId(userId);
        List<UUID> productIds = items.stream().map(WishlistItem::getProductId).toList();
        Map<UUID, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return items.stream()
                .map(item -> productsById.get(item.getProductId()))
                .filter(Objects::nonNull)
                .map(productMapper::toSummary)
                .toList();
    }

    @Transactional
    public void add(UUID userId, UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            return;
        }
        wishlistItemRepository.save(WishlistItem.builder().userId(userId).productId(productId).build());
    }

    @Transactional
    public void remove(UUID userId, UUID productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
