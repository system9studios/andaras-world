package com.andara.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test application configuration for integration tests.
 */
@SpringBootApplication(scanBasePackages = {
    "com.andara.api",
    "com.andara.application",
    "com.andara.infrastructure",
    "com.andara.content"
})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
