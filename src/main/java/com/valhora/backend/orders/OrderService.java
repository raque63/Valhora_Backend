package com.valhora.backend.orders;

import com.valhora.backend.cart.CartService;
import com.valhora.backend.cart.dto.CartItemResponse;
import com.valhora.backend.cart.dto.CartResponse;
import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.orders.dto.AdminOrderUpdateRequest;
import com.valhora.backend.orders.dto.OrderItemResponse;
import com.valhora.backend.orders.dto.OrderResponse;
import com.valhora.backend.orders.dto.OrderSummaryResponse;
import com.valhora.backend.users.User;
import com.valhora.backend.users.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CartService cartService, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createFromCart(UUID userId, String shippingAddress, String customerNote) {
        CartResponse cart = cartService.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.RECEIVED)
                .shippingAddress(blankToNull(shippingAddress))
                .customerNote(blankToNull(customerNote))
                .itemsSubtotal(cart.subtotal())
                .total(cart.subtotal())
                .build();

        for (CartItemResponse cartItem : cart.items()) {
            order.addItem(OrderItem.builder()
                    .productId(cartItem.productId())
                    .productName(cartItem.name())
                    .sku(cartItem.sku())
                    .unitPrice(cartItem.price())
                    .quantity(cartItem.quantity())
                    .lineSubtotal(cartItem.subtotal())
                    .build());
        }

        Order saved = orderRepository.save(order);
        cartService.clear(userId);

        User user = userRepository.findById(userId).orElse(null);
        return toResponse(saved, user);
    }

    public Page<OrderSummaryResponse> adminSearch(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);

        Map<UUID, User> usersById = loadUsers(orders.getContent());

        return orders.map(order -> new OrderSummaryResponse(
                order.getId(),
                order.getStatus(),
                customerName(usersById.get(order.getUserId())),
                order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(),
                order.getTotal(),
                order.getCreatedAt()));
    }

    public OrderResponse adminFindById(UUID id) {
        Order order = getOrThrow(id);
        User user = userRepository.findById(order.getUserId()).orElse(null);
        return toResponse(order, user);
    }

    @Transactional
    public OrderResponse adminUpdate(UUID id, AdminOrderUpdateRequest request) {
        Order order = getOrThrow(id);

        if (request.status() != null) {
            order.setStatus(request.status());
        }
        if (request.shippingCost() != null) {
            order.setShippingCost(request.shippingCost());
            order.setTotal(order.getItemsSubtotal().add(request.shippingCost()));
        }

        Order saved = orderRepository.save(order);
        User user = userRepository.findById(saved.getUserId()).orElse(null);
        return toResponse(saved, user);
    }

    private Order getOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }

    private Map<UUID, User> loadUsers(List<Order> orders) {
        List<UUID> userIds = orders.stream().map(Order::getUserId).distinct().toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private String customerName(User user) {
        return user != null ? user.getName() : "Usuario eliminado";
    }

    private OrderResponse toResponse(Order order, User user) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getSku(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                customerName(user),
                user != null ? user.getEmail() : null,
                order.getShippingAddress(),
                order.getCustomerNote(),
                items,
                order.getItemsSubtotal(),
                order.getShippingCost(),
                order.getTotal(),
                order.getCreatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
