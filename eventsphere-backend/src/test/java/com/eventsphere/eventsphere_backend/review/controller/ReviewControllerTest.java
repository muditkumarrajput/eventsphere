package com.eventsphere.eventsphere_backend.review.controller;

import com.eventsphere.eventsphere_backend.review.dto.CreateReviewRequest;
import com.eventsphere.eventsphere_backend.review.dto.ReviewResponse;
import com.eventsphere.eventsphere_backend.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest {

    private MockMvc mockMvc;

    private ReviewService reviewService;

    private ObjectMapper objectMapper;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        reviewService = mock(ReviewService.class);

        objectMapper = new ObjectMapper();

        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("user@test.com");

        ReviewController reviewController =
                new ReviewController(reviewService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(reviewController)
                .build();
    }


    // =========================================================
    // CREATE REVIEW
    // =========================================================

    @Test
    void shouldCreateReview() throws Exception {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);
        request.setRating(5);
        request.setComment("Excellent workshop!");

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(5L)
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .build();

        when(reviewService.createReview(
                any(CreateReviewRequest.class),
                eq("user@test.com")
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/reviews")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.eventId").value(3))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(
                        jsonPath("$.comment")
                                .value("Excellent workshop!")
                );

        verify(reviewService).createReview(
                any(CreateReviewRequest.class),
                eq("user@test.com")
        );
    }


    // =========================================================
    // GET EVENT REVIEWS
    // =========================================================

    @Test
    void shouldGetEventReviews() throws Exception {

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(5L)
                        .eventId(3L)
                        .rating(5)
                        .comment("Excellent workshop!")
                        .build();

        when(reviewService.getEventReviews(3L))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/reviews/event/3")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].eventId").value(3))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(
                        jsonPath("$[0].comment")
                                .value("Excellent workshop!")
                );

        verify(reviewService)
                .getEventReviews(3L);
    }


    // =========================================================
    // GET MY REVIEWS
    // =========================================================

    @Test
    void shouldGetMyReviews() throws Exception {

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(5L)
                        .eventId(3L)
                        .rating(4)
                        .comment("Very good workshop!")
                        .build();

        when(reviewService.getMyReviews("user@test.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/reviews/my")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(
                        jsonPath("$[0].comment")
                                .value("Very good workshop!")
                );

        verify(reviewService)
                .getMyReviews("user@test.com");
    }


    // =========================================================
    // GET AVERAGE RATING
    // =========================================================

    @Test
    void shouldGetAverageRating() throws Exception {

        when(reviewService.getAverageRating(3L))
                .thenReturn(4.5);

        mockMvc.perform(
                        get("/api/reviews/event/3/average")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$").value(4.5)
                );

        verify(reviewService)
                .getAverageRating(3L);
    }


    // =========================================================
    // UPDATE REVIEW
    // =========================================================

    @Test
    void shouldUpdateReview() throws Exception {

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setEventId(3L);
        request.setRating(4);
        request.setComment("Very good workshop!");

        ReviewResponse response =
                ReviewResponse.builder()
                        .id(5L)
                        .userId(5L)
                        .eventId(3L)
                        .rating(4)
                        .comment("Very good workshop!")
                        .build();

        when(reviewService.updateReview(
                eq(5L),
                any(CreateReviewRequest.class),
                eq("user@test.com")
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/reviews/5")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(
                        jsonPath("$.comment")
                                .value("Very good workshop!")
                );

        verify(reviewService).updateReview(
                eq(5L),
                any(CreateReviewRequest.class),
                eq("user@test.com")
        );
    }


    // =========================================================
    // DELETE REVIEW
    // =========================================================

    @Test
    void shouldDeleteReview() throws Exception {

        mockMvc.perform(
                        delete("/api/reviews/5")
                                .principal(authentication)
                )
                .andExpect(status().isOk());

        verify(reviewService)
                .deleteReview(
                        5L,
                        "user@test.com"
                );
    }
}