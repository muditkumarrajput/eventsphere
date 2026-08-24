package com.eventsphere.eventsphere_backend.auth.security;

import com.eventsphere.eventsphere_backend.common.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAccessDeniedHandler
        implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        ErrorResponse error =
                ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(
                                HttpStatus.FORBIDDEN.value()
                        )
                        .error(
                                HttpStatus.FORBIDDEN
                                        .getReasonPhrase()
                        )
                        .message("Access Denied.")
                        .path(
                                request.getRequestURI()
                        )
                        .build();

        response.setStatus(
                HttpStatus.FORBIDDEN.value()
        );

        response.setContentType(
                "application/json"
        );

        new ObjectMapper()
                .writeValue(
                        response.getOutputStream(),
                        error
                );
    }
}