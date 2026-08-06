package com.eventsphere.eventsphere_backend.review.mapper;

import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .eventId(review.getEvent().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}