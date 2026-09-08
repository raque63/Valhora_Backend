package com.valhora.backend.orders;

import com.valhora.backend.auth.UserPrincipal;
import com.valhora.backend.orders.dto.CreateOrderRequest;
import com.valhora.backend.orders.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody(required = false) CreateOrderRequest request) {
        CreateOrderRequest body = request != null ? request : new CreateOrderRequest(null, null);
        return orderService.createFromCart(principal.getUser().getId(), body.shippingAddress(), body.customerNote());
    }
}
