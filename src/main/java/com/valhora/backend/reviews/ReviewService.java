package com.valhora.backend.reviews;

import com.valhora.backend.common.exception.ResourceNotFoundException;
import com.valhora.backend.reviews.dto.ReviewAdminResponse;
import com.valhora.backend.reviews.dto.ReviewRequest;
import com.valhora.backend.reviews.dto.ReviewResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private static final int PUBLIC_REVIEWS_LIMIT = 30;
    private static final int ADMIN_REVIEWS_LIMIT = 100;

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewRepository reviewRepository, ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.reviewMapper = reviewMapper;
    }

    @Transactional
    public ReviewResponse submit(UUID userId, String authorName, ReviewRequest request) {
        Review review = Review.builder()
                .userId(userId)
                .authorName(authorName)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.PENDING)
                .build();
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    public List<ReviewResponse> getApproved() {
        Pageable pageable = PageRequest.of(0, PUBLIC_REVIEWS_LIMIT);
        return reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.APPROVED, pageable).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    public List<ReviewAdminResponse> listPending() {
        Pageable pageable = PageRequest.of(0, ADMIN_REVIEWS_LIMIT);
        return reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING, pageable).stream()
                .map(reviewMapper::toAdminResponse)
                .toList();
    }

    public List<ReviewAdminResponse> listAll() {
        Pageable pageable = PageRequest.of(0, ADMIN_REVIEWS_LIMIT);
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable).stream()
                .map(reviewMapper::toAdminResponse)
                .toList();
    }

    @Transactional
    public ReviewAdminResponse approve(UUID id) {
        Review review = getOrThrow(id);
        review.setStatus(ReviewStatus.APPROVED);
        return reviewMapper.toAdminResponse(review);
    }

    @Transactional
    public ReviewAdminResponse reject(UUID id) {
        Review review = getOrThrow(id);
        review.setStatus(ReviewStatus.REJECTED);
        return reviewMapper.toAdminResponse(review);
    }

    @Transactional
    public void delete(UUID id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reseña no encontrada");
        }
        reviewRepository.deleteById(id);
    }

    private Review getOrThrow(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));
    }
}
