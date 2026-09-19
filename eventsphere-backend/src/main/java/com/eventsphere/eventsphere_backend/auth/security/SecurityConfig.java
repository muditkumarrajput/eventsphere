package com.eventsphere.eventsphere_backend.auth.security;

import com.eventsphere.eventsphere_backend.auth.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // =====================================================
                // CORS
                // =====================================================
                .cors(Customizer.withDefaults())

                // =====================================================
                // CSRF
                // =====================================================
                .csrf(csrf -> csrf.disable())

                // =====================================================
                // STATELESS SESSION
                // =====================================================
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =====================================================
                // EXCEPTION HANDLING
                // =====================================================
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                        .accessDeniedHandler(
                                jwtAccessDeniedHandler
                        )
                )

                // =====================================================
                // AUTHORIZATION
                // =====================================================
                .authorizeHttpRequests(auth -> auth

                        // -------------------------------------------------
                        // CORS preflight requests
                        // -------------------------------------------------
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // -------------------------------------------------
                        // Swagger / OpenAPI
                        // These endpoints must be public so Swagger UI
                        // itself can load without a JWT.
                        // -------------------------------------------------
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // -------------------------------------------------
                        // Public Authentication APIs
                        // -------------------------------------------------
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // -------------------------------------------------
                        // Public Event GET APIs
                        // -------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/events/**"
                        ).permitAll()

                        // -------------------------------------------------
                        // Public Actuator Health Endpoint
                        // Used by deployment platforms and monitoring tools
                        // to check whether the application is healthy.
                        // -------------------------------------------------
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // -------------------------------------------------
                        // Everything else requires authentication
                        // -------------------------------------------------
                        .anyRequest().authenticated()
                )

                // =====================================================
                // AUTHENTICATION PROVIDER
                // =====================================================
                .authenticationProvider(
                        authenticationProvider()
                )

                // =====================================================
                // JWT FILTER
                // =====================================================
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // =====================================================
                // HTTP BASIC
                // =====================================================
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // =========================================================
    // AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return provider;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}