package com.eventsphere.eventsphere_backend.favorite.controller;

import com.eventsphere.eventsphere_backend.favorite.dto.FavoriteResponse;
import com.eventsphere.eventsphere_backend.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "Favorites",
        description = "APIs for managing a user's favorite events"
)
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // =========================================================
    // ADD EVENT TO FAVORITES
    // =========================================================

    @PostMapping("/{eventId}")
    @Operation(
            summary = "Add event to favorites",
            description = "Adds an event to the authenticated user's favorite list"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event added to favorites successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
    public ResponseEntity<Void> addFavorite(
            @PathVariable Long eventId,
            Authentication authentication) {

        favoriteService.addFavorite(
                eventId,
                authentication.getName()
        );

        return ResponseEntity.ok().build();
    }

    // =========================================================
    // REMOVE EVENT FROM FAVORITES
    // =========================================================

    @DeleteMapping("/{eventId}")
    @Operation(
            summary = "Remove event from favorites",
            description = "Removes an event from the authenticated user's favorite list"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Event removed from favorites successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Favorite or event not found"
            )
    })
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long eventId,
            Authentication authentication) {

        favoriteService.removeFavorite(
                eventId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // GET MY FAVORITES
    // =========================================================

    @GetMapping
    @Operation(
            summary = "Get my favorite events",
            description = "Returns all events favorited by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Favorite events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
    public List<FavoriteResponse> getMyFavorites(
            Authentication authentication) {

        return favoriteService.getMyFavorites(
                authentication.getName()
        );
    }

    // =========================================================
    // CHECK FAVORITE
    // =========================================================

    @GetMapping("/{eventId}")
    @Operation(
            summary = "Check if event is favorited",
            description = "Checks whether the authenticated user has added the specified event to favorites"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Favorite status retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
    public boolean isFavorite(
            @PathVariable Long eventId,
            Authentication authentication) {

        return favoriteService.isFavorite(
                eventId,
                authentication.getName()
        );
    }
}