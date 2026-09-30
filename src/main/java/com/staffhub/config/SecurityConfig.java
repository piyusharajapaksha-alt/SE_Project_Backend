package com.staffhub.config;

import com.staffhub.security.AuthUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
    // SECURITY FILTER CHAIN
    // ==========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // ------------------------------------------------
                // CORS
                // ------------------------------------------------

                .cors(cors -> {})

                // ------------------------------------------------
                // CSRF
                // ------------------------------------------------
                //
                // StaffHub uses custom JSON authentication endpoints.
                //
                // Authentication endpoints are excluded because:
                //
                // POST /api/auth/login
                // POST /api/auth/register
                // POST /api/auth/logout
                //
                // are handled directly by AuthController.
                //
                // All other application APIs remain CSRF protected.
                // ------------------------------------------------

                .csrf(csrf -> csrf

                        .csrfTokenRepository(
                                CookieCsrfTokenRepository
                                        .withHttpOnlyFalse()
                        )

                        .ignoringRequestMatchers(
                                "/api/auth/**"
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
                                "/api/auth/logout",
                                "/api/auth/csrf"
                        ).permitAll()

                        // Everything else requires login
                        .anyRequest().authenticated()
                )

                // ------------------------------------------------
                // CUSTOM JSON AUTH
                // ------------------------------------------------

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                // ------------------------------------------------
                // RETURN 401 FOR UNAUTHENTICATED API REQUESTS
                // ------------------------------------------------
                //
                // This changes:
                //
                // /api/auth/me -> 403
                //
                // into:
                //
                // /api/auth/me -> 401
                //
                // when nobody is logged in.
                // ------------------------------------------------

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                );

        return http.build();
    }
}