package com.eventsphere.eventsphere_backend.favorite.repository;

import com.eventsphere.eventsphere_backend.favorite.entity.Favorite;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndEvent(User user, Event event);

    List<Favorite> findByUser(User user);

    boolean existsByUserAndEvent(User user, Event event);
}