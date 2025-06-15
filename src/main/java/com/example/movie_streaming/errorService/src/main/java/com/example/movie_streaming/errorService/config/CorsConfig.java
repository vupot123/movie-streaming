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
                        .allowedOriginPatterns("*") // Cho phép tất cả các origin (sử dụng pattern thay vì "*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD") // Tất cả các phương thức HTTP
                        .allowedHeaders("*") // Cho phép tất cả header
                        .allowCredentials(true) // Cho phép gửi thông tin xác thực (JWT, cookie, v.v.)
                        .maxAge(3600); // Thời gian cache pre-flight request (1 giờ)
            }
        };
    }
}