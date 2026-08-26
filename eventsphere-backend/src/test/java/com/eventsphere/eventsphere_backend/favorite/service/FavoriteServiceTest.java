package com.eventsphere.eventsphere_backend.favorite.service;

import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.favorite.dto.FavoriteResponse;
import com.eventsphere.eventsphere_backend.favorite.entity.Favorite;
import com.eventsphere.eventsphere_backend.favorite.repository.FavoriteRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private FavoriteService favoriteService;


    // =========================================================
    // ADD FAVORITE - SUCCESS
    // =========================================================

    @Test
    void shouldAddFavorite() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(favoriteRepository.existsByUserAndEvent(user, event))
                .thenReturn(false);

        // Act
        favoriteService.addFavorite(3L, email);

        // Assert
        verify(favoriteRepository)
                .save(any(Favorite.class));
    }


    // =========================================================
    // ADD FAVORITE - DUPLICATE
    // =========================================================

    @Test
    void shouldNotAddDuplicateFavorite() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(favoriteRepository.existsByUserAndEvent(user, event))
                .thenReturn(true);

        // Act
        favoriteService.addFavorite(3L, email);

        // Assert
        verify(favoriteRepository, never())
                .save(any(Favorite.class));
    }


    // =========================================================
    // REMOVE FAVORITE
    // =========================================================

    @Test
    void shouldRemoveFavorite() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        Favorite favorite = Favorite.builder()
                .id(4L)
                .user(user)
                .event(event)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(favoriteRepository.findByUserAndEvent(user, event))
                .thenReturn(Optional.of(favorite));

        // Act
        favoriteService.removeFavorite(3L, email);

        // Assert
        verify(favoriteRepository)
                .delete(favorite);
    }


    // =========================================================
    // GET MY FAVORITES
    // =========================================================

    @Test
    void shouldGetMyFavorites() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);
        event.setTitle("Spring Boot Masterclass");
        event.setLocation("Bangalore");

        Favorite favorite = Favorite.builder()
                .id(4L)
                .user(user)
                .event(event)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(favoriteRepository.findByUser(user))
                .thenReturn(List.of(favorite));

        // Act
        List<FavoriteResponse> result =
                favoriteService.getMyFavorites(email);

        // Assert
        assertEquals(1, result.size());
        assertEquals(4L, result.get(0).getId());
        assertEquals(3L, result.get(0).getEventId());
        assertEquals(
                "Spring Boot Masterclass",
                result.get(0).getEventTitle()
        );
        assertEquals(
                "Bangalore",
                result.get(0).getEventLocation()
        );

        verify(favoriteRepository)
                .findByUser(user);
    }


    // =========================================================
    // CHECK FAVORITE - TRUE
    // =========================================================

    @Test
    void shouldReturnTrueWhenEventIsFavorite() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(favoriteRepository.existsByUserAndEvent(user, event))
                .thenReturn(true);

        // Act
        boolean result =
                favoriteService.isFavorite(3L, email);

        // Assert
        assertTrue(result);
    }


    // =========================================================
    // CHECK FAVORITE - FALSE
    // =========================================================

    @Test
    void shouldReturnFalseWhenEventIsNotFavorite() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(favoriteRepository.existsByUserAndEvent(user, event))
                .thenReturn(false);

        // Act
        boolean result =
                favoriteService.isFavorite(3L, email);

        // Assert
        assertFalse(result);
    }
}