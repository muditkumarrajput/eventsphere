package com.eventsphere.eventsphere_backend.event.specification;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventSpecification {

    private EventSpecification() {
        // Utility class
    }

    // Search title OR description
    public static Specification<Event> keywordContains(String keyword) {

        return (root, query, criteriaBuilder) -> {

            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String searchKeyword = "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            searchKeyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            searchKeyword
                    )
            );
        };
    }

    // Filter by category
    public static Specification<Event> hasCategory(
            EventCategory category) {

        return (root, query, criteriaBuilder) -> {

            if (category == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("category"),
                    category
            );
        };
    }

    // Filter by location
    public static Specification<Event> hasLocation(
            String location) {

        return (root, query, criteriaBuilder) -> {

            if (location == null || location.isBlank()) {
                return null;
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")),
                    "%" + location.toLowerCase() + "%"
            );
        };
    }

    // Minimum ticket price
    public static Specification<Event> priceGreaterThanOrEqual(
            BigDecimal minPrice) {

        return (root, query, criteriaBuilder) -> {

            if (minPrice == null) {
                return null;
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("ticketPrice"),
                    minPrice
            );
        };
    }

    // Maximum ticket price
    public static Specification<Event> priceLessThanOrEqual(
            BigDecimal maxPrice) {

        return (root, query, criteriaBuilder) -> {

            if (maxPrice == null) {
                return null;
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("ticketPrice"),
                    maxPrice
            );
        };
    }

    // Events from this date/time
    public static Specification<Event> eventDateAfter(
            LocalDateTime dateTime) {

        return (root, query, criteriaBuilder) -> {

            if (dateTime == null) {
                return null;
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("eventDate"),
                    dateTime
            );
        };
    }

    // Events until this date/time
    public static Specification<Event> eventDateBefore(
            LocalDateTime dateTime) {

        return (root, query, criteriaBuilder) -> {

            if (dateTime == null) {
                return null;
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("eventDate"),
                    dateTime
            );
        };
    }
}