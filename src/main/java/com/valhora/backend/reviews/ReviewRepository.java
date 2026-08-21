package com.valhora.backend.reviews;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    List<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
