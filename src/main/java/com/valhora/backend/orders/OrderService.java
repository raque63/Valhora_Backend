package com.valhora.backend.orders;

import com.valhora.backend.cart.CartService;
import com.valhora.backend.cart.dto.CartItemResponse;
import com.valhora.backend.cart.dto.CartResponse;
import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.common.storage.CloudinaryService;
import com.valhora.backend.notifications.EmailService;
import com.valhora.backend.notifications.WhatsAppService;
import com.valhora.backend.orders.dto.AdminOrderUpdateRequest;
import com.valhora.backend.orders.dto.CreateOrderRequest;
import com.valhora.backend.orders.dto.OrderItemResponse;
import com.valhora.backend.orders.dto.OrderResponse;
import com.valhora.backend.orders.dto.OrderSummaryResponse;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    private final JdbcTemplate jdbcTemplate;

    public OrderService(
            OrderRepository orderRepository,
            CartService cartService,
            CloudinaryService cloudinaryService,
            EmailService emailService,
            WhatsAppService whatsAppService,
            JdbcTemplate jdbcTemplate) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
        this.whatsAppService = whatsAppService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void ensureOrderNumberSequence() {
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1000 INCREMENT BY 1");
    }

    private Long nextOrderNumber() {
        return jdbcTemplate.queryForObject("SELECT nextval('order_number_seq')", Long.class);
    }

    @Transactional
    public OrderResponse createFromCart(UUID userId, CreateOrderRequest request) {
        CartResponse cart = cartService.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        Order order = Order.builder()
                .orderNumber(nextOrderNumber())
                .userId(userId)
                .status(OrderStatus.RECEIVED)
                .customerName(request.customerName())
                .customerPhone(request.customerPhone())
                .customerEmail(request.customerEmail())
                .province(request.province())
                .canton(request.canton())
                .district(request.district())
                .deliveryMethod(request.deliveryMethod())
                .paymentMethod(request.paymentMethod())
                .shippingAddress(blankToNull(request.shippingAddress()))
                .customerNote(blankToNull(request.customerNote()))
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

        emailService.sendNewOrderToAdmin(saved);
        whatsAppService.notifyAdminNewOrder(saved);

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse uploadPaymentProof(UUID userId, UUID orderId, MultipartFile file) {
        Order order = getOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Pedido no encontrado");
        }
        order.setPaymentProofUrl(cloudinaryService.uploadPaymentProof(file));
        Order saved = orderRepository.save(order);
        emailService.sendProofReceivedToAdmin(saved);
        whatsAppService.notifyAdminProofUploaded(saved);
        return toResponse(saved);
    }

    public List<OrderSummaryResponse> findMyOrders(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(order -> new OrderSummaryResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getStatus(),
                        order.getCustomerName(),
                        order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(),
                        order.getTotal(),
                        order.getCreatedAt()))
                .toList();
    }

    public OrderResponse findMyOrderById(UUID userId, UUID orderId) {
        Order order = getOrThrow(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Pedido no encontrado");
        }
        return toResponse(order);
    }

    public Page<OrderSummaryResponse> adminSearch(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);

        return orders.map(order -> new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getCustomerName(),
                order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(),
                order.getTotal(),
                order.getCreatedAt()));
    }

    public OrderResponse adminFindById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public OrderResponse adminUpdate(UUID id, AdminOrderUpdateRequest request) {
        Order order = getOrThrow(id);
        boolean justConfirmed = request.status() == OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.CONFIRMED;

        if (request.status() != null) {
            order.setStatus(request.status());
        }
        if (request.shippingCost() != null) {
            order.setShippingCost(request.shippingCost());
            order.setTotal(order.getItemsSubtotal().add(request.shippingCost()));
        }

        Order saved = orderRepository.save(order);

        if (justConfirmed) {
            emailService.sendPaymentConfirmedToCustomer(saved);
        }

        return toResponse(saved);
    }

    private Order getOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }

    private OrderResponse toResponse(Order order) {
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
                order.getOrderNumber(),
                order.getStatus(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerEmail(),
                order.getProvince(),
                order.getCanton(),
                order.getDistrict(),
                order.getDeliveryMethod(),
                order.getPaymentMethod(),
                order.getPaymentProofUrl(),
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
