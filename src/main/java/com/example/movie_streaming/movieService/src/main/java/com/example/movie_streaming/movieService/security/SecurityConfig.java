package com.example.movie_streaming.movieService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.movie_streaming.common.security.JwtAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Thêm cấu hình CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (không cần token)
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        .requestMatchers("/api/movies/generate-token").permitAll()
                        // Các yêu cầu GET tới các endpoint movies không cần token
                        .requestMatchers(HttpMethod.GET, "/api/movies").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        // Allow filter & search without token
                        .requestMatchers(HttpMethod.POST, "/api/movies/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies/search").permitAll()
                        // Allow addview for USER or ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/movies/*/view").hasAnyRole("USER", "ADMIN")
                        // Endpoints cần token và role ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                        // Tất cả các request khác cần xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // Cho phép tất cả origin (có thể giới hạn nếu cần)
        configuration.addAllowedMethod("*"); // Cho phép tất cả method (GET, POST, PUT, DELETE, v.v.)
        configuration.addAllowedHeader("*"); // Cho phép tất cả header
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}