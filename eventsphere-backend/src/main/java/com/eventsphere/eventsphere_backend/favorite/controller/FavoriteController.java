package com.eventsphere.eventsphere_backend.favorite.controller;

import com.eventsphere.eventsphere_backend.favorite.dto.FavoriteResponse;
import com.eventsphere.eventsphere_backend.favorite.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Add Event to Favorites
    @PostMapping("/{eventId}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable Long eventId,
            Authentication authentication) {

        favoriteService.addFavorite(
                eventId,
                authentication.getName()
        );

        return ResponseEntity.ok().build();
    }

    // Remove Event from Favorites
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long eventId,
            Authentication authentication) {

        favoriteService.removeFavorite(
                eventId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // Get My Favorites
    @GetMapping
    public List<FavoriteResponse> getMyFavorites(
            Authentication authentication) {

        return favoriteService.getMyFavorites(
                authentication.getName()
        );
    }

    // Check Favorite
    @GetMapping("/{eventId}")
    public boolean isFavorite(
            @PathVariable Long eventId,
            Authentication authentication) {

        return favoriteService.isFavorite(
                eventId,
                authentication.getName()
        );
    }
}