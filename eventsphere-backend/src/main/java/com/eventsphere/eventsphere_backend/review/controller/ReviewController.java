package com.eventsphere.eventsphere_backend.review.controller;

import com.eventsphere.eventsphere_backend.review.dto.CreateReviewRequest;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(
        name = "Reviews",
        description = "APIs for creating, viewing, updating and deleting event reviews"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // =========================================================
    // CREATE REVIEW
    // =========================================================

    @PostMapping
    @Operation(
            summary = "Create a review",
            description = "Creates a review for an event by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid review request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event or related resource not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Review already exists or review is not allowed"
            )
    })
    public ReviewResponse createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        return reviewService.createReview(
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // GET EVENT REVIEWS
    // =========================================================

    @GetMapping("/event/{eventId}")
    @Operation(
            summary = "Get reviews for an event",
            description = "Returns all reviews associated with the specified event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event reviews retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
    public List<ReviewResponse> getEventReviews(
            @PathVariable Long eventId) {

        return reviewService.getEventReviews(eventId);
    }

    // =========================================================
    // GET MY REVIEWS
    // =========================================================

    @GetMapping("/my")
    @Operation(
            summary = "Get my reviews",
            description = "Returns all reviews created by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User reviews retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
    public List<ReviewResponse> getMyReviews(
            Authentication authentication) {

        return reviewService.getMyReviews(
                authentication.getName()
        );
    }

    // =========================================================
    // GET AVERAGE RATING
    // =========================================================

    @GetMapping("/event/{eventId}/average")
    @Operation(
            summary = "Get event average rating",
            description = "Returns the average rating for the specified event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Average rating retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
    public Double getAverageRating(
            @PathVariable Long eventId) {

        return reviewService.getAverageRating(eventId);
    }

    // =========================================================
    // UPDATE REVIEW
    // =========================================================

    @PutMapping("/{reviewId}")
    @Operation(
            summary = "Update my review",
            description = "Updates a review belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid review request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not own the review"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found"
            )
    })
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        return reviewService.updateReview(
                reviewId,
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // DELETE REVIEW
    // =========================================================

    @DeleteMapping("/{reviewId}")
    @Operation(
            summary = "Delete my review",
            description = "Deletes a review belonging to the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Review deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not own the review"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found"
            )
    })
    public void deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        reviewService.deleteReview(
                reviewId,
                authentication.getName()
        );
    }
}