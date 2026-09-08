package com.valhora.backend.cart;

import com.valhora.backend.auth.UserPrincipal;
import com.valhora.backend.cart.dto.CartResponse;
import com.valhora.backend.cart.dto.UpdateCartItemRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return cartService.getCart(principal.getUser().getId());
    }

    @PostMapping("/{productId}")
    public CartResponse add(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID productId) {
        return cartService.addOrIncrement(principal.getUser().getId(), productId);
    }

    @PutMapping("/{productId}")
    public CartResponse updateQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateQuantity(principal.getUser().getId(), productId, request.quantity());
    }

    @DeleteMapping("/{productId}")
    public CartResponse remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID productId) {
        return cartService.remove(principal.getUser().getId(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clear(principal.getUser().getId());
    }
}
