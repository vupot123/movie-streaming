package com.example.movie_streaming.userService.controller;

import com.example.movie_streaming.common.exceptions.InvalidCredentialsException;
import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.userService.model.dto.response.JwtResponse;
import com.example.movie_streaming.userService.service.UserService;
import com.example.movie_streaming.common.security.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng ký người dùng thành công", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @RequestBody(required = false) LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtProvider.validateToken(token)) {
                String username = jwtProvider.getUsernameFromToken(token);
                String role = jwtProvider.getRoleFromToken(token);
                JwtResponse jwtResponse = JwtResponse.builder()
                        .accessToken(token)
                        .username(username)
                        .role(role)
                        .build();
                return ResponseEntity.ok(new ApiResponse<>(200, "Tự động đăng nhập thành công", jwtResponse));
            }
        }

        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Thiếu thông tin đăng nhập", null));
        }

        JwtResponse jwtResponse = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng nhập thành công", jwtResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (!jwtProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token không hợp lệ hoặc đã hết hạn", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(200, "Đăng xuất thành công", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> getMe(HttpServletRequest request) {
        String token = extractToken(request);
        if (!jwtProvider.validateToken(token)) {
            throw new InvalidCredentialsException("Token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtProvider.getUsernameFromToken(token);
        String role = jwtProvider.getRoleFromToken(token);

        Map<String, String> userInfo = Map.of(
                "username", username,
                "role", role
        );

        return ResponseEntity.ok(new ApiResponse<>(200, "Authenticated", userInfo));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<String>> addFavorite(HttpServletRequest request,
                                                           @Valid @RequestBody FavoriteRequest favoriteRequest) {
        String username = extractUsernameFromRequest(request);
        userService.addFavorite(username, favoriteRequest);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thêm phim yêu thích thành công", null));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFavorites(HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        List<Map<String, Object>> favorites = userService.getFavorites(username);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách phim yêu thích thành công", favorites));
    }

    @PostMapping("/views")
    public ResponseEntity<ApiResponse<String>> recordMovieView(HttpServletRequest request,
                                                               @RequestBody Map<String, Long> body) {
        Long movieId = body.get("movieId");
        if (movieId == null) {
            throw new IllegalArgumentException("movieId là bắt buộc");
        }

        String username = extractUsernameFromRequest(request);
        userService.recordMovieView(username, movieId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Ghi lại lượt xem phim thành công", null));
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Token không hợp lệ hoặc thiếu");
        }

        String token = authHeader.substring(7);
        if (!jwtProvider.validateToken(token)) {
            throw new InvalidCredentialsException("Token không hợp lệ hoặc đã hết hạn");
        }

        String username = jwtProvider.getUsernameFromToken(token);
        if (username == null) {
            throw new InvalidCredentialsException("Không thể trích xuất username từ token");
        }

        return username;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Token không hợp lệ hoặc thiếu");
        }
        return authHeader.substring(7);
    }
}
