package com.example.movie_streaming.userService.controller;

import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.userService.model.dto.response.ApiResponse;
import com.example.movie_streaming.userService.model.dto.response.JwtResponse;
import com.example.movie_streaming.userService.model.entity.Favorite;
import com.example.movie_streaming.userService.service.UserService;
import com.example.movie_streaming.userService.security.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "User registered successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", new JwtResponse(token)));
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<String>> addFavorite(HttpServletRequest request, @RequestBody FavoriteRequest favoriteRequest) {
        String username = extractUsernameFromRequest(request);
        userService.addFavorite(username, favoriteRequest);
        return ResponseEntity.ok(new ApiResponse<>(200, "Favorite added successfully", null));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<Favorite>>> getFavorites(HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        List<Favorite> favorites = userService.getFavorites(username);
        return ResponseEntity.ok(new ApiResponse<>(200, "Favorites fetched successfully", favorites));
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return JwtProvider.getUsernameFromToken(token);
    }
}

