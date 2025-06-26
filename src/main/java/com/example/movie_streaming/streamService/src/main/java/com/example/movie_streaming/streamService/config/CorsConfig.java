package com.example.movie_streaming.errorService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép tất cả origin bằng pattern
        config.addAllowedOriginPattern("*"); // Thay setAllowedOrigins bằng pattern để hỗ trợ allowCredentials

        // Cho phép tất cả phương thức HTTP
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Cho phép tất cả header
        config.setAllowedHeaders(List.of("*"));

        // Bật credentials để hỗ trợ gửi token
        config.setAllowCredentials(true);

        // Thời gian cache pre-flight request
        config.setMaxAge(3600L);

        // Áp dụng cho tất cả endpoint
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}