package com.eventsphere.eventsphere_backend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public abstract class AbstractPostgresIntegrationTest {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("eventsphere_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        postgres.start();
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "create-drop"
        );

        registry.add(
                "jwt.secret",
                () -> "test-secret-key-that-is-at-least-32-characters-long"
        );
    }

    @BeforeEach
    void cleanDatabase() {

        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    payments,
                    bookings,
                    favorites,
                    reviews,
                    notifications,
                    events,
                    users
                RESTART IDENTITY CASCADE
                """);
    }
}