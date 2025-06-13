package com.example.movie_streaming.userService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả các endpoint
                        .allowedOrigins("*") // Cho phép tất cả các origin (các domain/cổng)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Cho phép tất cả các phương thức HTTP
                        .allowedHeaders("*") // Cho phép tất cả các header
                        .allowCredentials(true) // Cho phép gửi cookie hoặc thông tin xác thực nếu cần
                        .maxAge(3600); // Thời gian cache pre-flight request (tính bằng giây)
            }
        };
    }
}