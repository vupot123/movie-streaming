package com.example.movie_streaming.userService.security;

import com.example.movie_streaming.common.security.JwtAuthenticationFilter;
import com.example.movie_streaming.common.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import BCryptPasswordEncoder
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
        return new JwtAuthenticationFilter(jwtProvider); // ✅ Inject JwtProvider
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Khai báo bean BCryptPasswordEncoder
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Bật CORS
                .csrf(csrf -> csrf.disable()) // Tắt CSRF nếu không cần
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập không cần xác thực cho các endpoint userService
                        .requestMatchers("/api/user/register", "/api/user/login").permitAll()
                        // Cấp quyền cho USER và ADMIN truy cập các endpoint mới
                        .requestMatchers(HttpMethod.GET, "/api/user/favorites").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/user/detail").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/user").hasAnyRole("USER", "ADMIN") // Cập nhật thông tin user
                        .requestMatchers(HttpMethod.POST, "/api/user/views").hasAnyRole("USER", "ADMIN") // Ghi lại lượt xem
                        .requestMatchers(HttpMethod.GET, "/api/user/me").hasAnyRole("USER", "ADMIN") // Lấy thông tin cá nhân
                        // Bảo vệ các endpoint streamService yêu cầu vai trò ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/upload/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/upload/file").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/upload/files").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/upload/files/search").hasRole("ADMIN")
                        // Cho phép OPTIONS cho tất cả các endpoint để hỗ trợ pre-flight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Yêu cầu xác thực cho các request khác
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

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
}