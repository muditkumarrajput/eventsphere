package com.eventsphere.eventsphere_backend.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.eventsphere.eventsphere_backend.common.exception.GlobalExceptionHandler;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.event.entity.EventStatus;
import com.eventsphere.eventsphere_backend.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private EventService eventService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private EventController eventController;

    private LocalValidatorFactoryBean validator;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        objectMapper.registerModule(
                new JavaTimeModule()
        );

        validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(eventController)
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .setValidator(validator)
                        .build();
    }


    // =========================================================
    // CREATE EVENT
    // =========================================================

    @Test
    void shouldCreateEvent()
            throws Exception {

        when(authentication.getName())
                .thenReturn("organizer@test.com");

        CreateEventRequest request =
                new CreateEventRequest();

        request.setTitle("Java Workshop");

        request.setDescription(
                "Spring Boot Workshop"
        );

        request.setLocation("Delhi");

        request.setEventDate(
                LocalDateTime.of(
                        2026,
                        12,
                        20,
                        10,
                        0
                )
        );

        request.setCapacity(100);

        request.setTicketPrice(
                new BigDecimal("999")
        );

        request.setCategory(
                EventCategory.WORKSHOP
        );

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .description(
                                "Spring Boot Workshop"
                        )
                        .location("Delhi")
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("999")
                        )
                        .category(
                                EventCategory.WORKSHOP
                        )
                        .build();

        when(
                eventService.createEvent(
                        any(CreateEventRequest.class),
                        eq("organizer@test.com")
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/events")
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Java Workshop"
                                )
                )
                .andExpect(
                        jsonPath("$.location")
                                .value("Delhi")
                )
                .andExpect(
                        jsonPath("$.capacity")
                                .value(100)
                )
                .andExpect(
                        jsonPath("$.ticketPrice")
                                .value(999)
                );

        verify(eventService)
                .createEvent(
                        any(CreateEventRequest.class),
                        eq("organizer@test.com")
                );
    }


    // =========================================================
    // GET ALL EVENTS
    // =========================================================

    @Test
    void shouldGetAllEvents()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(eventService.getAllEvents())
                .thenReturn(
                        List.of(response)
                );

        mockMvc.perform(
                        get("/api/events")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$[0].title")
                                .value(
                                        "Java Workshop"
                                )
                );

        verify(eventService)
                .getAllEvents();
    }


    // =========================================================
    // SEARCH EVENTS
    // =========================================================

    @Test
    void shouldSearchEvents()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(
                eventService.searchEvents("Java")
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/api/events/search")
                                .param(
                                        "keyword",
                                        "Java"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$[0].title")
                                .value(
                                        "Java Workshop"
                                )
                );

        verify(eventService)
                .searchEvents("Java");
    }


    // =========================================================
    // CATEGORY
    // =========================================================

    @Test
    void shouldGetEventsByCategory()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(
                eventService.getEventsByCategory(
                        EventCategory.WORKSHOP
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get(
                                "/api/events/category/WORKSHOP"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
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
    // LOCATION
    // =========================================================

    @Test
    void shouldGetEventsByLocation()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(
                eventService.getEventsByLocation(
                        "Delhi"
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get(
                                "/api/events/location/Delhi"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getEventsByLocation("Delhi");
    }


    // =========================================================
    // DATE
    // =========================================================

    @Test
    void shouldGetEventsByDate()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(
                eventService.getEventsByDate(
                        any()
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get(
                                "/api/events/date/2026-12-20"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getEventsByDate(any());
    }


    // =========================================================
    // PRICE
    // =========================================================

    @Test
    void shouldGetEventsByPriceRange()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(
                eventService.getEventsByPriceRange(
                        new BigDecimal("500"),
                        new BigDecimal("1500")
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/api/events/price")
                                .param(
                                        "minPrice",
                                        "500"
                                )
                                .param(
                                        "maxPrice",
                                        "1500"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getEventsByPriceRange(
                        new BigDecimal("500"),
                        new BigDecimal("1500")
                );
    }


    // =========================================================
    // UPCOMING EVENTS
    // =========================================================

    @Test
    void shouldGetUpcomingEvents()
            throws Exception {

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(eventService.getUpcomingEvents())
                .thenReturn(
                        List.of(response)
                );

        mockMvc.perform(
                        get("/api/events/upcoming")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getUpcomingEvents();
    }


    // =========================================================
    // GET MY EVENTS
    // =========================================================

    @Test
    void shouldGetMyEvents()
            throws Exception {

        when(authentication.getName())
                .thenReturn("organizer@test.com");

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(
                eventService.getMyEvents(
                        "organizer@test.com"
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/api/events/my-events")
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8)
                );

        verify(eventService)
                .getMyEvents(
                        "organizer@test.com"
                );
    }


    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @Test
    void shouldGetEventById()
            throws Exception {

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
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Java Workshop"
                                )
                );

        verify(eventService)
                .getEventById(8L);
    }


    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @Test
    void shouldUpdateEvent()
            throws Exception {

        when(authentication.getName())
                .thenReturn("organizer@test.com");

        UpdateEventRequest request =
                new UpdateEventRequest();

        request.setTitle(
                "Updated Workshop"
        );

        request.setDescription(
                "Updated Description"
        );

        request.setLocation("Delhi");

        request.setEventDate(
                LocalDateTime.of(
                        2026,
                        12,
                        30,
                        10,
                        0
                )
        );

        request.setCapacity(100);

        request.setTicketPrice(
                new BigDecimal("1499")
        );

        request.setCategory(
                EventCategory.WORKSHOP
        );

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title(
                                "Updated Workshop"
                        )
                        .location("Delhi")
                        .build();

        when(
                eventService.updateEvent(
                        eq(8L),
                        any(UpdateEventRequest.class),
                        eq("organizer@test.com")
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put("/api/events/8")
                                .principal(
                                        authentication
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Updated Workshop"
                                )
                )
                .andExpect(
                        jsonPath("$.location")
                                .value("Delhi")
                );

        verify(eventService)
                .updateEvent(
                        eq(8L),
                        any(UpdateEventRequest.class),
                        eq("organizer@test.com")
                );
    }


    // =========================================================
    // CANCEL EVENT
    // =========================================================

    @Test
    void shouldCancelEvent()
            throws Exception {

        when(authentication.getName())
                .thenReturn("organizer@test.com");

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .status(EventStatus.CANCELLED)
                        .build();

        when(
                eventService.cancelEvent(
                        eq(8L),
                        eq("organizer@test.com")
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch("/api/events/8/cancel")
                                .principal(
                                        authentication
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Java Workshop"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );

        verify(eventService)
                .cancelEvent(
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
                        get(
                                "/api/events/category/INVALID"
                        )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }
}