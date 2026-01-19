package com.andara.api.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to validate admin API token for protected endpoints.
 * Checks for X-Admin-Token header and validates against configured token.
 */
@Component
public class AdminAuthFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AdminAuthFilter.class);
    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";
    
    @Value("${andara.admin.token:#{null}}")
    private String adminToken;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        
        // Only check admin endpoints
        if (!requestPath.startsWith("/api/admin/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // If no admin token is configured, allow all (development mode)
        if (adminToken == null || adminToken.isBlank()) {
            log.warn("Admin token not configured - allowing request to {}", requestPath);
            chain.doFilter(request, response);
            return;
        }
        
        // Check for token in header
        String providedToken = httpRequest.getHeader(ADMIN_TOKEN_HEADER);
        
        if (providedToken == null || providedToken.isBlank()) {
            log.warn("Admin request without token: {} from {}", requestPath, httpRequest.getRemoteAddr());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Admin token required. Provide " + ADMIN_TOKEN_HEADER + " header.\"}");
            return;
        }
        
        // Validate token
        if (!adminToken.equals(providedToken)) {
            log.warn("Invalid admin token for: {} from {}", requestPath, httpRequest.getRemoteAddr());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Invalid admin token\"}");
            return;
        }
        
        // Token valid, proceed
        log.debug("Admin request authorized: {}", requestPath);
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (adminToken != null && !adminToken.isBlank()) {
            log.info("Admin authentication filter initialized with configured token");
        } else {
            log.warn("Admin authentication filter initialized without token - ALL ADMIN ENDPOINTS UNPROTECTED");
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
