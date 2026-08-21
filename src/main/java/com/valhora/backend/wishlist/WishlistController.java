package com.valhora.backend.wishlist;

import com.valhora.backend.auth.UserPrincipal;
import com.valhora.backend.products.dto.ProductSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<ProductSummaryResponse> getWishlist(@AuthenticationPrincipal UserPrincipal principal) {
        return wishlistService.getWishlist(principal.getUser().getId());
    }

    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID productId) {
        wishlistService.add(principal.getUser().getId(), productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID productId) {
        wishlistService.remove(principal.getUser().getId(), productId);
    }
}
