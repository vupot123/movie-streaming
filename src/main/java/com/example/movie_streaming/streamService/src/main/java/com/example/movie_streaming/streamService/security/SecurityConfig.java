package com.example.movie_streaming.streamService.security;

import com.example.movie_streaming.common.security.JwtAuthenticationFilter;
import com.example.movie_streaming.common.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Bảo vệ tất cả các endpoint của streamService yêu cầu vai trò ADMIN
                        .requestMatchers(HttpMethod.POST, "/upload/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/upload/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/upload/files").hasRole("ADMIN")
                        // Từ chối tất cả các yêu cầu khác không khớp
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}