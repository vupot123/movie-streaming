package com.example.movie_streaming.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtProvider jwtProvider;

    @Autowired
    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/user/login",
            "/api/user/register",
            "/swagger-ui/",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        logger.debug("Incoming request to path: {}", path);

        // Skip paths that don't require authentication
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("status", 403);
            errorBody.put("message", "Missing or invalid Authorization header");
            errorBody.put("data", null);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), errorBody);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtProvider.getUsernameFromToken(token);

            logger.debug("Authenticated user: {}", username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            logger.error("JWT validation error", e);

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("status", 403);
            errorBody.put("message", "Invalid or expired JWT token");
            errorBody.put("data", null);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getOutputStream(), errorBody);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
