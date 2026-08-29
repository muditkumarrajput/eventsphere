package com.eventsphere.eventsphere_backend.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private final String secret =
            "my-super-secret-key-for-jwt-testing-purpose-only-123456";

    private final long jwtExpiration =
            3600000L;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(
                secret,
                jwtExpiration
        );
    }

    // =========================================================
    // GENERATE TOKEN
    // =========================================================

    @Test
    void shouldGenerateToken() {

        String token = jwtService.generateToken(
                "user@example.com"
        );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    // =========================================================
    // EXTRACT EMAIL
    // =========================================================

    @Test
    void shouldExtractEmailFromToken() {

        String email = "user@example.com";

        String token = jwtService.generateToken(email);

        String extractedEmail =
                jwtService.extractEmail(token);

        assertEquals(
                email,
                extractedEmail
        );
    }

    // =========================================================
    // EXTRACT CLAIM
    // =========================================================

    @Test
    void shouldExtractClaimFromToken() {

        String email = "user@example.com";

        String token = jwtService.generateToken(email);

        String subject = jwtService.extractClaim(
                token,
                Claims -> Claims.getSubject()
        );

        assertEquals(
                email,
                subject
        );
    }

    // =========================================================
    // VALID TOKEN
    // =========================================================

    @Test
    void shouldValidateTokenWhenEmailMatchesAndTokenIsNotExpired() {

        String email = "user@example.com";

        String token = jwtService.generateToken(email);

        boolean result =
                jwtService.isTokenValid(
                        token,
                        email
                );

        assertTrue(result);
    }

    // =========================================================
    // WRONG EMAIL
    // =========================================================

    @Test
    void shouldReturnFalseWhenEmailDoesNotMatch() {

        String token = jwtService.generateToken(
                "user@example.com"
        );

        boolean result =
                jwtService.isTokenValid(
                        token,
                        "wrong@example.com"
                );

        assertFalse(result);
    }

    // =========================================================
    // EXPIRED TOKEN
    // =========================================================

    @Test
    void shouldReturnFalseWhenTokenIsExpired()
            throws Exception {

        String email = "user@example.com";

        JwtService expiredJwtService =
                new JwtService(
                        secret,
                        -1000L
                );

        String token =
                expiredJwtService.generateToken(email);

        Thread.sleep(50);

        boolean result =
                expiredJwtService.isTokenValid(
                        token,
                        email
                );

        assertFalse(result);
    }
}