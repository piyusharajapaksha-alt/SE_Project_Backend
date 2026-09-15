package com.staffhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                // Local computer
                                "http://localhost:5173",
                                "http://127.0.0.1:5173",

                                // FRONTEND_URL from environment/config
                                frontendUrl,

                                // Private LAN networks
                                "http://10.*.*.*:5173",
                                "http://172.*.*.*:5173",
                                "http://192.168.*.*:5173"
                        )
                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"
                        )
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}