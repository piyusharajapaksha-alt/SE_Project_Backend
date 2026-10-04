package com.staffhub.config;

import com.staffhub.security.AuthUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthUserDetailsService userDetailsService;


    public SecurityConfig(
            AuthUserDetailsService userDetailsService) {

        this.userDetailsService =
                userDetailsService;
    }


    // ============================================================
    // PASSWORD ENCODER
    // ============================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }


    // ============================================================
    // AUTHENTICATION PROVIDER
    // ============================================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService);

        provider.setPasswordEncoder(
                passwordEncoder);

        return provider;
    }


    // ============================================================
    // AUTHENTICATION MANAGER
    // ============================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration
                .getAuthenticationManager();
    }


    // ============================================================
    // SECURITY CONTEXT
    // ============================================================

    @Bean
    public SecurityContextRepository
    securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }


    // ============================================================
    // CSRF
    // ============================================================

    @Bean
    public HttpSessionCsrfTokenRepository
    csrfTokenRepository() {

        return new HttpSessionCsrfTokenRepository();
    }


    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            HttpSessionCsrfTokenRepository csrfTokenRepository)
            throws Exception {

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
                                csrfTokenRepository)

                        // Login and registration are intentionally
                        // excluded because they happen before the
                        // authenticated session is established.
                        .ignoringRequestMatchers(
                                "/api/auth/login",
                                "/api/auth/register")
                )


                // ------------------------------------------------
                // AUTHORIZATION
                // ------------------------------------------------
                .authorizeHttpRequests(auth -> auth

                        // ========================================
                        // PUBLIC AUTHENTICATION ENDPOINTS
                        // ========================================

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/csrf"
                        )
                        .permitAll()


                        // ========================================
                        // PUBLIC QR ATTENDANCE MONITOR
                        // ========================================
                        //
                        // The physical QR monitor does not have
                        // a StaffHub user account/session.
                        //
                        // Therefore the monitor must be able to
                        // request its monitor ID and activation
                        // code before it is authorized by HR.
                        //
                        // IMPORTANT:
                        // Only GET is public.
                        //
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/attendance/monitor"
                        )
                        .permitAll()


                        // ========================================
                        // EVERYTHING ELSE
                        // ========================================
                        //
                        // All other StaffHub APIs require an
                        // authenticated user/session.
                        //
                        .anyRequest()
                        .authenticated()
                )


                // ------------------------------------------------
                // FORM LOGIN
                // ------------------------------------------------
                .formLogin(form ->
                        form.disable())


                // ------------------------------------------------
                // HTTP BASIC
                // ------------------------------------------------
                .httpBasic(basic ->
                        basic.disable())


                // ------------------------------------------------
                // SECURITY CONTEXT
                // ------------------------------------------------
                .securityContext(securityContext ->

                        securityContext
                                .securityContextRepository(
                                        securityContextRepository)
                )


                // ------------------------------------------------
                // UNAUTHENTICATED RESPONSE
                // ------------------------------------------------
                //
                // Your frontend expects HTTP 401 instead of
                // Spring's default login-page redirect.
                //
                .exceptionHandling(exception ->

                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED))
                );


        return http.build();
    }
}

