package com.valhora.backend.reviews;

import com.valhora.backend.reviews.dto.ReviewAdminResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class ReviewAdminController {

    private final ReviewService reviewService;

    public ReviewAdminController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewAdminResponse> list(@RequestParam(name = "status", required = false) String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return reviewService.listPending();
        }
        return reviewService.listAll();
    }

    @PatchMapping("/{id}/approve")
    public ReviewAdminResponse approve(@PathVariable UUID id) {
        return reviewService.approve(id);
    }

    @PatchMapping("/{id}/reject")
    public ReviewAdminResponse reject(@PathVariable UUID id) {
        return reviewService.reject(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        reviewService.delete(id);
    }
}
