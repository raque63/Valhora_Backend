package com.valhora.backend.orders;

import com.valhora.backend.auth.UserPrincipal;
import com.valhora.backend.orders.dto.CreateOrderRequest;
import com.valhora.backend.orders.dto.OrderResponse;
import com.valhora.backend.orders.dto.OrderSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderSummaryResponse> myOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.findMyOrders(principal.getUser().getId());
    }

    @GetMapping("/{id}")
    public OrderResponse myOrderById(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return orderService.findMyOrderById(principal.getUser().getId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createFromCart(principal.getUser().getId(), request);
    }

    @PostMapping("/{id}/payment-proof")
    public OrderResponse uploadPaymentProof(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam MultipartFile file) {
        return orderService.uploadPaymentProof(principal.getUser().getId(), id, file);
    }
}
