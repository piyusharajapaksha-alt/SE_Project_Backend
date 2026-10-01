package com.staffhub.config;

import com.staffhub.security.AuthUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.http.HttpStatus;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthUserDetailsService userDetailsService;

    public SecurityConfig(
            AuthUserDetailsService userDetailsService
    ) {
        this.userDetailsService = userDetailsService;
    }

    // ==========================================================
    // PASSWORD ENCODER
    // ==========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    // ==========================================================
    // AUTHENTICATION PROVIDER
    // ==========================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // ==========================================================
    // AUTHENTICATION MANAGER
    // ==========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // ==========================================================
    // SECURITY CONTEXT REPOSITORY
    // ==========================================================

    @Bean
    public SecurityContextRepository securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }

    // ==========================================================
    // CSRF TOKEN REPOSITORY
    // ==========================================================
    //
    // StaffHub is a React SPA.
    //
    // We keep the CSRF token in the HTTP session and expose the
    // token through /api/auth/csrf.
    //
    // The React application then sends the token in:
    //
    // X-XSRF-TOKEN
    //
    // for POST / PUT / PATCH / DELETE requests.
    //
    // This avoids depending on JavaScript reading a backend
    // cookie across different frontend/backend origins.
    // ==========================================================

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {

        return new HttpSessionCsrfTokenRepository();
    }

    // ==========================================================
    // SECURITY FILTER CHAIN
    // ==========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            HttpSessionCsrfTokenRepository csrfTokenRepository
    ) throws Exception {

        http

                // ------------------------------------------------
                // CORS
                // ------------------------------------------------

                .cors(cors -> {})

                // ------------------------------------------------
                // CSRF
                // ------------------------------------------------

                .csrf(csrf -> csrf

                        .csrfTokenRepository(
                                csrfTokenRepository
                        )

                        // Authentication endpoints are intentionally
                        // excluded because login/register create the
                        // authenticated session.
                        .ignoringRequestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        )
                )

                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------

                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/csrf"
                        ).permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // ------------------------------------------------
                // DISABLE DEFAULT LOGIN MECHANISMS
                // ------------------------------------------------

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                // ------------------------------------------------
                // SECURITY CONTEXT
                // ------------------------------------------------

                .securityContext(securityContext ->

                        securityContext
                                .securityContextRepository(
                                        securityContextRepository
                                )
                )

                // ------------------------------------------------
                // UNAUTHENTICATED API RESPONSE
                // ------------------------------------------------

                .exceptionHandling(exception ->

                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                );

        return http.build();
    }
}