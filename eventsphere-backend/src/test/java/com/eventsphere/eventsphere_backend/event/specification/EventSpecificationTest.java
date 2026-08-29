package com.eventsphere.eventsphere_backend.event.specification;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventSpecificationTest {

    // =========================================================
    // KEYWORD
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenKeywordIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.keywordContains(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldReturnNullPredicateWhenKeywordIsBlank() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.keywordContains("   ");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateKeywordSearchPredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<String> titlePath = mock(Path.class);
        Path<String> descriptionPath = mock(Path.class);

        Expression<String> lowerTitle = mock(Expression.class);
        Expression<String> lowerDescription = mock(Expression.class);

        Predicate titlePredicate = mock(Predicate.class);
        Predicate descriptionPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("title"))
                .thenReturn(titlePath);

        when(root.<String>get("description"))
                .thenReturn(descriptionPath);

        when(criteriaBuilder.lower(titlePath))
                .thenReturn(lowerTitle);

        when(criteriaBuilder.lower(descriptionPath))
                .thenReturn(lowerDescription);

        when(criteriaBuilder.like(
                lowerTitle,
                "%concert%"
        )).thenReturn(titlePredicate);

        when(criteriaBuilder.like(
                lowerDescription,
                "%concert%"
        )).thenReturn(descriptionPredicate);

        when(criteriaBuilder.or(
                titlePredicate,
                descriptionPredicate
        )).thenReturn(finalPredicate);

        Specification<Event> specification =
                EventSpecification.keywordContains("Concert");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(finalPredicate, result);

        verify(root)
                .<String>get("title");

        verify(root)
                .<String>get("description");

        verify(criteriaBuilder)
                .lower(titlePath);

        verify(criteriaBuilder)
                .lower(descriptionPath);

        verify(criteriaBuilder)
                .like(
                        lowerTitle,
                        "%concert%"
                );

        verify(criteriaBuilder)
                .like(
                        lowerDescription,
                        "%concert%"
                );

        verify(criteriaBuilder)
                .or(
                        titlePredicate,
                        descriptionPredicate
                );
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenCategoryIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.hasCategory(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldCreateCategoryPredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<EventCategory> categoryPath =
                mock(Path.class);

        Predicate predicate =
                mock(Predicate.class);

        EventCategory category =
                EventCategory.CONCERT;

        when(root.<EventCategory>get("category"))
                .thenReturn(categoryPath);

        when(criteriaBuilder.equal(
                categoryPath,
                category
        )).thenReturn(predicate);

        Specification<Event> specification =
                EventSpecification.hasCategory(category);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(predicate, result);

        verify(root)
                .<EventCategory>get("category");

        verify(criteriaBuilder)
                .equal(
                        categoryPath,
                        category
                );
    }

    // =========================================================
    // LOCATION
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenLocationIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.hasLocation(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldReturnNullPredicateWhenLocationIsBlank() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.hasLocation("   ");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateLocationPredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<String> locationPath =
                mock(Path.class);

        Expression<String> lowerLocation =
                mock(Expression.class);

        Predicate predicate =
                mock(Predicate.class);

        when(root.<String>get("location"))
                .thenReturn(locationPath);

        when(criteriaBuilder.lower(locationPath))
                .thenReturn(lowerLocation);

        when(criteriaBuilder.like(
                lowerLocation,
                "%lucknow%"
        )).thenReturn(predicate);

        Specification<Event> specification =
                EventSpecification.hasLocation("Lucknow");

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(predicate, result);

        verify(root)
                .<String>get("location");

        verify(criteriaBuilder)
                .lower(locationPath);

        verify(criteriaBuilder)
                .like(
                        lowerLocation,
                        "%lucknow%"
                );
    }

    // =========================================================
    // MINIMUM PRICE
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenMinimumPriceIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.priceGreaterThanOrEqual(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldCreateMinimumPricePredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<BigDecimal> pricePath =
                mock(Path.class);

        Predicate predicate =
                mock(Predicate.class);

        BigDecimal minPrice =
                new BigDecimal("500.00");

        when(root.<BigDecimal>get("ticketPrice"))
                .thenReturn(pricePath);

        when(criteriaBuilder.greaterThanOrEqualTo(
                pricePath,
                minPrice
        )).thenReturn(predicate);

        Specification<Event> specification =
                EventSpecification.priceGreaterThanOrEqual(
                        minPrice
                );

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(predicate, result);

        verify(root)
                .<BigDecimal>get("ticketPrice");

        verify(criteriaBuilder)
                .greaterThanOrEqualTo(
                        pricePath,
                        minPrice
                );
    }

    // =========================================================
    // MAXIMUM PRICE
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenMaximumPriceIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.priceLessThanOrEqual(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldCreateMaximumPricePredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<BigDecimal> pricePath =
                mock(Path.class);

        Predicate predicate =
                mock(Predicate.class);

        BigDecimal maxPrice =
                new BigDecimal("2000.00");

        when(root.<BigDecimal>get("ticketPrice"))
                .thenReturn(pricePath);

        when(criteriaBuilder.lessThanOrEqualTo(
                pricePath,
                maxPrice
        )).thenReturn(predicate);

        Specification<Event> specification =
                EventSpecification.priceLessThanOrEqual(
                        maxPrice
                );

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(predicate, result);

        verify(root)
                .<BigDecimal>get("ticketPrice");

        verify(criteriaBuilder)
                .lessThanOrEqualTo(
                        pricePath,
                        maxPrice
                );
    }

    // =========================================================
    // EVENT DATE AFTER
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenEventDateAfterIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.eventDateAfter(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldCreateEventDateAfterPredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<LocalDateTime> datePath =
                mock(Path.class);

        Predicate predicate =
                mock(Predicate.class);

        LocalDateTime dateTime =
                LocalDateTime.of(
                        2026,
                        8,
                        29,
                        10,
                        30
                );

        when(root.<LocalDateTime>get("eventDate"))
                .thenReturn(datePath);

        when(criteriaBuilder.greaterThanOrEqualTo(
                datePath,
                dateTime
        )).thenReturn(predicate);

        Specification<Event> specification =
                EventSpecification.eventDateAfter(
                        dateTime
                );

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(predicate, result);

        verify(root)
                .<LocalDateTime>get("eventDate");

        verify(criteriaBuilder)
                .greaterThanOrEqualTo(
                        datePath,
                        dateTime
                );
    }

    // =========================================================
    // EVENT DATE BEFORE
    // =========================================================

    @Test
    void shouldReturnNullPredicateWhenEventDateBeforeIsNull() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Specification<Event> specification =
                EventSpecification.eventDateBefore(null);

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertNull(result);

        verifyNoInteractions(root);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    void shouldCreateEventDateBeforePredicate() {

        Root<Event> root = mock(Root.class);
        CriteriaQuery<Event> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<LocalDateTime> datePath =
                mock(Path.class);

        Predicate predicate =
                mock(Predicate.class);

        LocalDateTime dateTime =
                LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                );

        when(root.<LocalDateTime>get("eventDate"))
                .thenReturn(datePath);

        when(criteriaBuilder.lessThanOrEqualTo(
                datePath,
                dateTime
        )).thenReturn(predicate);

        Specification<Event> specification =
                EventSpecification.eventDateBefore(
                        dateTime
                );

        Predicate result =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(predicate, result);

        verify(root)
                .<LocalDateTime>get("eventDate");

        verify(criteriaBuilder)
                .lessThanOrEqualTo(
                        datePath,
                        dateTime
                );
    }
}