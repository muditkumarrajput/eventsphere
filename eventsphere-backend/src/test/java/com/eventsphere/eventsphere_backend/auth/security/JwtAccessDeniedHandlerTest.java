package com.eventsphere.eventsphere_backend.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class JwtAccessDeniedHandlerTest {

    private JwtAccessDeniedHandler handler;
    private ObjectMapper objectMapper;

    private HttpServletRequest request;
    private HttpServletResponse response;

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() throws Exception {

        objectMapper = new ObjectMapper();

        handler = new JwtAccessDeniedHandler(objectMapper);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        outputStream = new ByteArrayOutputStream();

        when(request.getRequestURI())
                .thenReturn("/api/events/10");

        when(response.getOutputStream())
                .thenReturn(
                        new jakarta.servlet.ServletOutputStream() {

                            @Override
                            public void write(int b) {
                                outputStream.write(b);
                            }

                            @Override
                            public boolean isReady() {
                                return true;
                            }

                            @Override
                            public void setWriteListener(
                                    jakarta.servlet.WriteListener writeListener) {
                            }
                        }
                );
    }

    @Test
    void shouldHandleAccessDeniedException() throws Exception {

        AccessDeniedException exception =
                new AccessDeniedException("Access denied");

        handler.handle(
                request,
                response,
                exception
        );

        verify(response)
                .setStatus(403);

        verify(response)
                .setContentType("application/json");

        verify(request)
                .getRequestURI();

        verify(response)
                .getOutputStream();

        String jsonResponse =
                outputStream.toString();

        assertTrue(
                jsonResponse.contains("\"status\":403")
        );

        assertTrue(
                jsonResponse.contains("\"error\":\"Forbidden\"")
        );

        assertTrue(
                jsonResponse.contains(
                        "\"message\":\"You do not have permission to access this resource\""
                )
        );

        assertTrue(
                jsonResponse.contains(
                        "\"path\":\"/api/events/10\""
                )
        );
    }
}