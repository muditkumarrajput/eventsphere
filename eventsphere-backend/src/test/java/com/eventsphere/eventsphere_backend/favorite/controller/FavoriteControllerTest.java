package com.eventsphere.eventsphere_backend.favorite.controller;

import com.eventsphere.eventsphere_backend.favorite.dto.FavoriteResponse;
import com.eventsphere.eventsphere_backend.favorite.service.FavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FavoriteControllerTest {

    private MockMvc mockMvc;

    private FavoriteService favoriteService;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        favoriteService = mock(FavoriteService.class);

        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("user@test.com");

        FavoriteController favoriteController =
                new FavoriteController(favoriteService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(favoriteController)
                .build();
    }


    // =========================================================
    // ADD FAVORITE
    // =========================================================

    @Test
    void shouldAddFavorite() throws Exception {

        mockMvc.perform(
                        post("/api/favorites/8")
                                .principal(authentication)
                )
                .andExpect(status().isOk());

        verify(favoriteService)
                .addFavorite(
                        8L,
                        "user@test.com"
                );
    }


    // =========================================================
    // REMOVE FAVORITE
    // =========================================================

    @Test
    void shouldRemoveFavorite() throws Exception {

        mockMvc.perform(
                        delete("/api/favorites/8")
                                .principal(authentication)
                )
                .andExpect(status().isNoContent());

        verify(favoriteService)
                .removeFavorite(
                        8L,
                        "user@test.com"
                );
    }


    // =========================================================
    // GET MY FAVORITES
    // =========================================================

    @Test
    void shouldGetMyFavorites() throws Exception {

        FavoriteResponse response =
                FavoriteResponse.builder()
                        .id(5L)
                        .eventId(8L)
                        .build();

        when(favoriteService.getMyFavorites(
                "user@test.com"
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/favorites")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                        .andExpect(
                                jsonPath("$[0].id")
                                        .value(5)
                        )
                        .andExpect(
                                jsonPath("$[0].eventId")
                                        .value(8)
                        );

        verify(favoriteService)
                .getMyFavorites(
                        "user@test.com"
                );
    }


    // =========================================================
    // CHECK FAVORITE
    // =========================================================

    @Test
    void shouldCheckFavorite() throws Exception {

        when(favoriteService.isFavorite(
                8L,
                "user@test.com"
        )).thenReturn(true);

        mockMvc.perform(
                        get("/api/favorites/8")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$")
                                .value(true)
                );

        verify(favoriteService)
                .isFavorite(
                        8L,
                        "user@test.com"
                );
    }
}
