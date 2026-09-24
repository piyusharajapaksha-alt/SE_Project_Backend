package com.staffhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StaffhubBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                StaffhubBackendApplication.class,
                args
        );
    }
}