package com.staffhub.config;

import com.staffhub.security.AuthUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

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

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration
                .getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository
    securityContextRepository() {

        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public HttpSessionCsrfTokenRepository
    csrfTokenRepository() {

        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            HttpSessionCsrfTokenRepository csrfTokenRepository)
            throws Exception {

        http

            .cors(cors -> {})

            .csrf(csrf -> csrf

                    .csrfTokenRepository(
                            csrfTokenRepository)

                    .ignoringRequestMatchers(
                            "/api/auth/login",
                            "/api/auth/register")
            )

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/csrf")
                    .permitAll()

                    .anyRequest()
                    .authenticated()
            )

            .formLogin(form ->
                    form.disable())

            .httpBasic(basic ->
                    basic.disable())

            .securityContext(securityContext ->

                    securityContext
                            .securityContextRepository(
                                    securityContextRepository)
            )

            .exceptionHandling(exception ->

                    exception.authenticationEntryPoint(
                            new HttpStatusEntryPoint(
                                    HttpStatus.UNAUTHORIZED))
            );

        return http.build();
    }
}