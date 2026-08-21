package com.valhora.backend.reviews;

import com.valhora.backend.reviews.dto.ReviewAdminResponse;
import com.valhora.backend.reviews.dto.ReviewResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewResponse toResponse(Review review);

    ReviewAdminResponse toAdminResponse(Review review);
}
