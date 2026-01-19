package com.andara.api.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for the API layer.
 * Registers the AdminAuthFilter to protect /api/admin/** endpoints.
 */
@Configuration
public class SecurityConfig {

    /**
     * Register AdminAuthFilter for admin endpoint protection.
     * 
     * @param adminAuthFilter The admin authentication filter
     * @return Filter registration bean
     */
    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilterRegistration(AdminAuthFilter adminAuthFilter) {
        FilterRegistrationBean<AdminAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(adminAuthFilter);
        registration.addUrlPatterns("/api/admin/*");
        registration.setName("adminAuthFilter");
        registration.setOrder(1); // Execute early in filter chain
        return registration;
    }
}
