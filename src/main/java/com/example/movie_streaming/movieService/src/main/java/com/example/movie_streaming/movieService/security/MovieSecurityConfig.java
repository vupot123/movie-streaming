package com.example.movie_streaming.movieService.security;

import com.example.movie_streaming.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class MovieSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public MovieSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain movieSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // Dùng corsConfigurationSource() đã được cấu hình trong common
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/user/login", "/api/user/register", "/api/movies/generate-token").permitAll()

                        // Các yêu cầu GET không cần token
                        .requestMatchers(HttpMethod.GET, "/api/movies", "/api/movies/**", "/api/movies/search").permitAll()

                        // POST filter search không cần token
                        .requestMatchers(HttpMethod.POST, "/api/movies/filter").permitAll()

                        // POST add view cần USER hoặc ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/movies/*/view").hasAnyRole("USER", "ADMIN")

                        // ADMIN quyền sửa
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")

                        // Tất cả các endpoint còn lại yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
