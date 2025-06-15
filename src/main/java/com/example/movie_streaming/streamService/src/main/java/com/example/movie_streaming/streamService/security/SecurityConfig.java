package com.example.movie_streaming.streamService.security;

import com.example.movie_streaming.common.security.JwtAuthenticationFilter;
import com.example.movie_streaming.common.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Bật CORS
                .csrf(csrf -> csrf.disable()) // Tắt CSRF nếu không cần
                .authorizeHttpRequests(auth -> auth
                        // Bảo vệ các endpoint của streamService yêu cầu vai trò ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/upload/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/upload/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/upload/files").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/upload/files/search").hasRole("ADMIN")
                        // Cho phép OPTIONS cho tất cả các endpoint để hỗ trợ pre-flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Từ chối các yêu cầu khác không khớp
                        .anyRequest().authenticated() // Thay .denyAll() bằng .authenticated() để cho phép nếu đã xác thực
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:5183",
                "http://127.0.0.1:5500",
                "https://movie-streaming-stream-service-319946458144.asia-southeast1.run.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Quan trọng cho credentials: "include"
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}