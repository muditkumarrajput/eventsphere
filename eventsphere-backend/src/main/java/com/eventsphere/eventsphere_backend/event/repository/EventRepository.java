package com.eventsphere.eventsphere_backend.event.repository;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

}