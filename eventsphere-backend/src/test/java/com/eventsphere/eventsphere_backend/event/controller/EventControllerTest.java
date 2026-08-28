package com.eventsphere.eventsphere_backend.event.controller;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.event.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    private MockMvc mockMvc;

    private EventService eventService;

    private ObjectMapper objectMapper;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        eventService = mock(EventService.class);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("organizer@test.com");

        EventController eventController =
                new EventController(eventService);

        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver();

        mockMvc = MockMvcBuilders
                .standaloneSetup(eventController)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
    }


    // =========================================================
    // CREATE EVENT
    // =========================================================

    @Test
    void shouldCreateEvent() throws Exception {

        CreateEventRequest request =
                new CreateEventRequest();

        request.setTitle("Java Workshop");
        request.setDescription("Spring Boot Workshop");
        request.setLocation("Mumbai");
        request.setEventDate(
                LocalDateTime.of(2026, 12, 20, 10, 0)
        );
        request.setCapacity(100);
        request.setTicketPrice(
                new BigDecimal("999")
        );
        request.setCategory(EventCategory.WORKSHOP);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .description("Spring Boot Workshop")
                        .location("Mumbai")
                        .eventDate(
                                LocalDateTime.of(
                                        2026,
                                        12,
                                        20,
                                        10,
                                        0
                                )
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("999")
                        )
                        .category(EventCategory.WORKSHOP)
                        .build();

        when(eventService.createEvent(
                any(CreateEventRequest.class),
                eq("organizer@test.com")
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/events")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(
                        jsonPath("$.title")
                                .value("Java Workshop")
                )
                .andExpect(
                        jsonPath("$.location")
                                .value("Mumbai")
                )
                .andExpect(
                        jsonPath("$.capacity")
                                .value(100)
                )
                .andExpect(
                        jsonPath("$.ticketPrice")
                                .value(999)
                );

        verify(eventService).createEvent(
                any(CreateEventRequest.class),
                eq("organizer@test.com")
        );
    }


    // =========================================================
    // GET ALL EVENTS
    // =========================================================

    @Test
    void shouldGetAllEvents() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(eventService.getAllEvents())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Java Workshop")
                );

        verify(eventService).getAllEvents();
    }


    // =========================================================
    // SEARCH EVENTS
    // =========================================================

    @Test
    void shouldSearchEvents() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(eventService.searchEvents("Java"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/search")
                                .param("keyword", "Java")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Java Workshop")
                );

        verify(eventService)
                .searchEvents("Java");
    }


    // =========================================================
    // FILTER BY CATEGORY
    // =========================================================

    @Test
    void shouldGetEventsByCategory() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .category(EventCategory.WORKSHOP)
                        .build();

        when(eventService.getEventsByCategory(
                EventCategory.WORKSHOP
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/category/WORKSHOP")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getEventsByCategory(
                        EventCategory.WORKSHOP
                );
    }


    // =========================================================
    // FILTER BY LOCATION
    // =========================================================

    @Test
    void shouldGetEventsByLocation() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .location("Mumbai")
                        .build();

        when(eventService.getEventsByLocation("Mumbai"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/location/Mumbai")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].location")
                                .value("Mumbai")
                );

        verify(eventService)
                .getEventsByLocation("Mumbai");
    }


    // =========================================================
    // FILTER BY DATE
    // =========================================================

    @Test
    void shouldGetEventsByDate() throws Exception {

        LocalDate date =
                LocalDate.of(2026, 12, 20);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(eventService.getEventsByDate(date))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/date/2026-12-20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getEventsByDate(date);
    }


    // =========================================================
    // FILTER BY PRICE
    // =========================================================

    @Test
    void shouldGetEventsByPriceRange() throws Exception {

        BigDecimal minPrice =
                new BigDecimal("500");

        BigDecimal maxPrice =
                new BigDecimal("1500");

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .ticketPrice(
                                new BigDecimal("999")
                        )
                        .build();

        when(eventService.getEventsByPriceRange(
                minPrice,
                maxPrice
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/price")
                                .param("minPrice", "500")
                                .param("maxPrice", "1500")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].ticketPrice")
                                .value(999)
                );

        verify(eventService)
                .getEventsByPriceRange(
                        minPrice,
                        maxPrice
                );
    }


    // =========================================================
    // UPCOMING EVENTS
    // =========================================================

    @Test
    void shouldGetUpcomingEvents() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Upcoming Workshop")
                        .build();

        when(eventService.getUpcomingEvents())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/upcoming")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Upcoming Workshop")
                );

        verify(eventService)
                .getUpcomingEvents();
    }


    // =========================================================
    // MY EVENTS
    // =========================================================

    @Test
    void shouldGetMyEvents() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("My Workshop")
                        .build();

        when(eventService.getMyEvents(
                "organizer@test.com"
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/events/my-events")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("My Workshop")
                );

        verify(eventService)
                .getMyEvents(
                        "organizer@test.com"
                );
    }


    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void shouldGetEventsWithPagination() throws Exception {

        Pageable pageable =
                PageRequest.of(0, 5);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        Page<EventResponse> page =
                new PageImpl<>(
                        List.of(response),
                        pageable,
                        1
                );

        when(eventService.getEvents(
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/events/page")
                                .param("page", "0")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.content[0].title")
                                .value("Java Workshop")
                );

        verify(eventService)
                .getEvents(any(Pageable.class));
    }


    // =========================================================
    // DYNAMIC EVENT FILTERING
    // =========================================================

    @Test
    void shouldFilterEvents() throws Exception {

        Pageable pageable =
                PageRequest.of(0, 5);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .category(EventCategory.WORKSHOP)
                        .location("Mumbai")
                        .ticketPrice(
                                new BigDecimal("999")
                        )
                        .eventDate(
                                LocalDateTime.of(
                                        2026,
                                        12,
                                        20,
                                        10,
                                        0
                                )
                        )
                        .build();

        Page<EventResponse> page =
                new PageImpl<>(
                        List.of(response),
                        pageable,
                        1
                );

        when(eventService.filterEvents(
                eq("Java"),
                eq(EventCategory.WORKSHOP),
                eq("Mumbai"),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("1500")),
                eq(LocalDateTime.of(
                        2026,
                        12,
                        1,
                        0,
                        0
                )),
                eq(LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                )),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/events/filter")
                                .param("keyword", "Java")
                                .param("category", "WORKSHOP")
                                .param("location", "Mumbai")
                                .param("minPrice", "500")
                                .param("maxPrice", "1500")
                                .param(
                                        "startDate",
                                        "2026-12-01T00:00:00"
                                )
                                .param(
                                        "endDate",
                                        "2026-12-31T23:59:00"
                                )
                                .param("page", "0")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.content[0].title")
                                .value("Java Workshop")
                )
                .andExpect(
                        jsonPath("$.content[0].location")
                                .value("Mumbai")
                )
                .andExpect(
                        jsonPath("$.content[0].ticketPrice")
                                .value(999)
                );

        verify(eventService).filterEvents(
                eq("Java"),
                eq(EventCategory.WORKSHOP),
                eq("Mumbai"),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("1500")),
                eq(LocalDateTime.of(
                        2026,
                        12,
                        1,
                        0,
                        0
                )),
                eq(LocalDateTime.of(
                        2026,
                        12,
                        31,
                        23,
                        59
                )),
                any(Pageable.class)
        );
    }


    // =========================================================
    // DYNAMIC FILTERING - NO PARAMETERS
    // =========================================================

    @Test
    void shouldFilterEventsWithoutParameters() throws Exception {

        Pageable pageable =
                PageRequest.of(0, 20);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        Page<EventResponse> page =
                new PageImpl<>(
                        List.of(response),
                        pageable,
                        1
                );

        when(eventService.filterEvents(
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/events/filter")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.content[0].title")
                                .value("Java Workshop")
                );

        verify(eventService).filterEvents(
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        );
    }


    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @Test
    void shouldGetEventById() throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(eventService.getEventById(8L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/events/8")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Java Workshop")
                );

        verify(eventService)
                .getEventById(8L);
    }


    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @Test
    void shouldUpdateEvent() throws Exception {

        UpdateEventRequest request =
                new UpdateEventRequest();

        request.setTitle("Updated Workshop");
        request.setDescription("Updated Description");
        request.setLocation("Delhi");
        request.setEventDate(
                LocalDateTime.of(
                        2026,
                        12,
                        25,
                        10,
                        0
                )
        );
        request.setCapacity(200);
        request.setTicketPrice(
                new BigDecimal("1499")
        );
        request.setCategory(EventCategory.WORKSHOP);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Updated Workshop")
                        .description("Updated Description")
                        .location("Delhi")
                        .eventDate(
                                LocalDateTime.of(
                                        2026,
                                        12,
                                        25,
                                        10,
                                        0
                                )
                        )
                        .capacity(200)
                        .ticketPrice(
                                new BigDecimal("1499")
                        )
                        .category(EventCategory.WORKSHOP)
                        .build();

        when(eventService.updateEvent(
                eq(8L),
                any(UpdateEventRequest.class),
                eq("organizer@test.com")
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/events/8")
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Updated Workshop")
                )
                .andExpect(
                        jsonPath("$.location")
                                .value("Delhi")
                );

        verify(eventService).updateEvent(
                eq(8L),
                any(UpdateEventRequest.class),
                eq("organizer@test.com")
        );
    }


    // =========================================================
    // DELETE EVENT
    // =========================================================

    @Test
    void shouldDeleteEvent() throws Exception {

        doNothing()
                .when(eventService)
                .deleteEvent(
                        eq(8L),
                        eq("organizer@test.com")
                );

        mockMvc.perform(
                        delete("/api/events/8")
                                .principal(authentication)
                )
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(
                eq(8L),
                eq("organizer@test.com")
        );
    }


    // =========================================================
    // INVALID CATEGORY
    // =========================================================

    @Test
    void shouldReturnBadRequestForInvalidCategory()
            throws Exception {

        mockMvc.perform(
                        get("/api/events/category/INVALID")
                )
                .andExpect(status().isBadRequest());
    }


    // =========================================================
    // INVALID DATE
    // =========================================================

    @Test
    void shouldReturnBadRequestForInvalidDate()
            throws Exception {

        mockMvc.perform(
                        get("/api/events/date/invalid-date")
                )
                .andExpect(status().isBadRequest());
    }
}