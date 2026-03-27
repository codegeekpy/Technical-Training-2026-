package com.anurag.events.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * CORS filter that runs at the highest priority — BEFORE Spring Security.
 * This ensures preflight OPTIONS requests and all responses carry correct
 * CORS headers even when Spring Security would otherwise reject the request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://technical-training-2026.vercel.app}")
    private String allowedOriginsRaw;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String origin = request.getHeader("Origin");
        boolean isOptions = "OPTIONS".equalsIgnoreCase(request.getMethod());

        if (origin != null) {
            List<String> allowed = Arrays.stream(allowedOriginsRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (allowed.contains(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods",
                        "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers",
                        "Authorization, Content-Type, X-Requested-With, Accept, Origin, " +
                        "Access-Control-Request-Method, Access-Control-Request-Headers");
                response.setHeader("Access-Control-Max-Age", "3600");

                // Short-circuit preflight for allowed origins
                if (isOptions) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            } else if (isOptions) {
                // Preflight from unknown origin — reject cleanly without CORS headers
                // so the browser can report a proper CORS block (not a network error)
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } else if (isOptions) {
            // OPTIONS with no Origin header (e.g. curl) — just OK
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
