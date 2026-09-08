package com.valhora.backend.orders;

import com.valhora.backend.orders.dto.AdminOrderUpdateRequest;
import com.valhora.backend.orders.dto.OrderResponse;
import com.valhora.backend.orders.dto.OrderSummaryResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class OrderAdminController {

    private final OrderService orderService;

    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderSummaryResponse> search(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderService.adminSearch(status, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable UUID id) {
        return orderService.adminFindById(id);
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable UUID id, @RequestBody AdminOrderUpdateRequest request) {
        return orderService.adminUpdate(id, request);
    }
}
