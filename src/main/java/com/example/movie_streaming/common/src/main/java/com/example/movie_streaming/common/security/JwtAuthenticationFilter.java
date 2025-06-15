package com.example.movie_streaming.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/movies/generate-token"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Bỏ qua kiểm tra token cho các endpoint không cần xác thực
        if (EXCLUDED_PATHS.contains(path) || isPublicEndpoint(request) || "OPTIONS".equals(request.getMethod())) {
            // Xử lý CORS pre-flight
            if ("OPTIONS".equals(request.getMethod())) {
                response.setStatus(HttpStatus.OK.value()); // Sử dụng HttpStatus.OK
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Access-Control-Max-Age", "3600");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, 403, "Missing or invalid Authorization header");
            return;
        }

        try {
            String token = authHeader.substring(7);
            if (!jwtProvider.validateToken(token)) {
                sendErrorResponse(response, 403, "Invalid or expired JWT token");
                return;
            }

            String username = jwtProvider.getUsernameFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);

            // Thiết lập thông tin xác thực với role
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendErrorResponse(response, 403, "Invalid or expired JWT token: " + e.getMessage());
        }
    }

    // Kiểm tra xem endpoint có phải là public không
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        return "GET".equals(method) && (
                path.matches("/api/movies(/\\d+)?") || // GET /api/movies, GET /api/movies/{id}
                        path.startsWith("/api/movies/filter") || // GET /api/movies/filter
                        path.startsWith("/api/movies/search")    // GET /api/movies/search
        );
    }

    // Gửi response lỗi với kiểm tra null
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message != null ? message : "Internal server error");
        error.put("data", null);
        response.setHeader("Access-Control-Allow-Origin", "*"); // Thêm header CORS cho response lỗi
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}