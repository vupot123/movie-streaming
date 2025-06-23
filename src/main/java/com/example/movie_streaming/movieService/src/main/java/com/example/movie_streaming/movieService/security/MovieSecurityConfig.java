package com.example.movie_streaming.movieService.security;

import com.example.movie_streaming.common.security.JwtAuthenticationFilter;
import com.example.movie_streaming.common.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class MovieSecurityConfig {

    private final JwtProvider jwtProvider;

    @Autowired
    public MovieSecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider); // Truyền JwtProvider
    }

    @Bean
    public SecurityFilterChain movieSecurityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.setSharedObject(HttpFirewall.class, allowUrlEncodedSlashHttpFirewall());

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // Enable CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/user/login", "/api/user/register", "/api/movies/generate-token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/collections/featured", "/api/collections/not-featured").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/movies/filter").permitAll()

                        // === ACTORS ===
                        .requestMatchers(HttpMethod.GET, "/api/actors").permitAll() // Gộp getAll và search
                        .requestMatchers(HttpMethod.GET, "/api/actors/{id}").permitAll() // Lấy actor theo ID
                        .requestMatchers(HttpMethod.POST, "/api/actors").hasRole("ADMIN") // Tạo actor
                        .requestMatchers(HttpMethod.PUT, "/api/actors/{id}").hasRole("ADMIN") // Cập nhật actor
                        .requestMatchers(HttpMethod.DELETE, "/api/actors/{id}").hasRole("ADMIN") // Xóa actor

                        // Authenticated for view tracking (specify clearly, avoid /**/views)
                        .requestMatchers(HttpMethod.POST, "/api/movies/{id}/views", "/api/movies/views").hasAnyRole("USER", "ADMIN")

                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers("/api/collections/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // Cho phép tất cả origin với pattern
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Hỗ trợ gửi token
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        firewall.setAllowSemicolon(true);
        return firewall;
    }
}