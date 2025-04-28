package com.example.userService.controller;

import com.example.userService.model.dto.request.FavoriteRequest;
import com.example.userService.model.dto.request.LoginRequest;
import com.example.userService.model.dto.request.RegisterRequest;
import com.example.userService.model.dto.response.JwtResponse;
import com.example.userService.model.entity.Favorite;
import com.example.userService.service.UserService;
import com.example.userService.security.JwtProvider;
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/favorites")
    public ResponseEntity<?> addFavorite(HttpServletRequest request, @RequestBody FavoriteRequest favoriteRequest) {
        String username = extractUsernameFromRequest(request);
        userService.addFavorite(username, favoriteRequest);
        return ResponseEntity.ok("Favorite added successfully");
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<Favorite>> getFavorites(HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        List<Favorite> favorites = userService.getFavorites(username);
        return ResponseEntity.ok(favorites);
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return JwtProvider.getUsernameFromToken(token);
    }
}
