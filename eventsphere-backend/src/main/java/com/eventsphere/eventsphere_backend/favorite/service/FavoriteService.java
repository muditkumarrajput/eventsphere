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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            EventRepository eventRepository) {

        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    // Add Event to Favorites
    public void addFavorite(Long eventId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(eventId));

        if (favoriteRepository.existsByUserAndEvent(user, event)) {
            return;
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .event(event)
                .build();

        favoriteRepository.save(favorite);
    }

    // Remove Event from Favorites
    public void removeFavorite(Long eventId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(eventId));

        favoriteRepository.findByUserAndEvent(user, event)
                .ifPresent(favoriteRepository::delete);
    }

    // Get My Favorites
    public List<FavoriteResponse> getMyFavorites(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return favoriteRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Check Favorite
    public boolean isFavorite(Long eventId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(eventId));

        return favoriteRepository.existsByUserAndEvent(user, event);
    }

    // Convert Favorite Entity to Response DTO
    private FavoriteResponse toResponse(Favorite favorite) {

        Event event = favorite.getEvent();

        return FavoriteResponse.builder()
                .id(favorite.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventLocation(event.getLocation())
                .eventDate(event.getEventDate())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}