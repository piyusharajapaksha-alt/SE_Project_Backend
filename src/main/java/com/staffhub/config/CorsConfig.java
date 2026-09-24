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
            public void addCorsMappings(
                    CorsRegistry registry
            ) {

                registry.addMapping("/**")

                        .allowedOriginPatterns(

                                // Local development
                                "http://localhost:*",
                                "http://127.0.0.1:*",

                                // Configured frontend
                                frontendUrl,

                                // LAN development
                                "http://10.*.*.*:*",
                                "http://172.*.*.*:*",
                                "http://192.168.*.*:*"
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

                        // IMPORTANT:
                        // Required for JSESSIONID and XSRF cookies.
                        .allowCredentials(true)

                        .maxAge(3600);
            }
        };
    }
}