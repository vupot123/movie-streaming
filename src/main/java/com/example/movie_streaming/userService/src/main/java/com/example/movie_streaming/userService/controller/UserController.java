package com.example.movie_streaming.userService.controller;

import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.userService.model.dto.response.JwtResponse;
import com.example.movie_streaming.userService.service.UserService;
import com.example.movie_streaming.common.security.JwtProvider;
import com.example.movie_streaming.common.exceptions.DuplicateResourceException;
import com.example.movie_streaming.common.exceptions.InvalidCredentialsException;
import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.debug("Yêu cầu đăng ký từ username: {}", request.getUsername());
            userService.register(request);
            logger.info("Đăng ký thành công cho username: {}", request.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(200, "Đăng ký người dùng thành công", null));
        } catch (DuplicateResourceException e) {
            logger.warn("Lỗi đăng ký: {}", e.getMessage());
            return ResponseEntity.status(409).body(new ApiResponse<>(409, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi đăng ký username: {}", request.getUsername(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.debug("Yêu cầu đăng nhập từ username: {}", request.getUsername());
            JwtResponse jwtResponse = userService.login(request);
            logger.info("Đăng nhập thành công cho username: {}", request.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(200, "Đăng nhập thành công", jwtResponse));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi đăng nhập: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi đăng nhập: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi đăng nhập username: {}", request.getUsername(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<String>> addFavorite(HttpServletRequest request, @Valid @RequestBody FavoriteRequest favoriteRequest) {
        try {
            logger.debug("Yêu cầu thêm phim yêu thích với movieId: {}", favoriteRequest.getMovieId());
            String username = extractUsernameFromRequest(request);
            userService.addFavorite(username, favoriteRequest);
            logger.info("Thêm phim yêu thích thành công cho username: {}, movieId: {}", username, favoriteRequest.getMovieId());
            return ResponseEntity.ok(new ApiResponse<>(200, "Thêm phim yêu thích thành công", null));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi thêm phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi thêm phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (DuplicateResourceException e) {
            logger.warn("Lỗi thêm phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(409).body(new ApiResponse<>(409, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi thêm phim yêu thích với movieId: {}", favoriteRequest.getMovieId(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getFavorites(HttpServletRequest request) {
        try {
            logger.debug("Yêu cầu lấy danh sách phim yêu thích");
            String username = extractUsernameFromRequest(request);
            List<Map<String, Object>> favorites = userService.getFavorites(username);
            logger.info("Lấy danh sách phim yêu thích thành công cho username: {}, số lượng: {}", username, favorites.size());
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách phim yêu thích thành công", favorites));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi lấy danh sách phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi lấy danh sách phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi lấy danh sách phim yêu thích", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @PostMapping("/views")
    public ResponseEntity<ApiResponse<String>> recordMovieView(HttpServletRequest request, @RequestBody Map<String, Long> body) {
        try {
            logger.debug("Yêu cầu ghi lại lượt xem phim với movieId: {}", body.get("movieId"));
            String username = extractUsernameFromRequest(request);
            Long movieId = body.get("movieId");
            if (movieId == null) {
                logger.warn("Thiếu movieId trong yêu cầu ghi lại lượt xem phim");
                throw new IllegalArgumentException("movieId là bắt buộc");
            }
            userService.recordMovieView(username, movieId);
            logger.info("Ghi lại lượt xem phim thành công cho username: {}, movieId: {}", username, movieId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Ghi lại lượt xem phim thành công", null));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi ghi lại lượt xem phim: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi ghi lại lượt xem phim: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            logger.warn("Lỗi ghi lại lượt xem phim: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi ghi lại lượt xem phim với movieId: {}", body.get("movieId"), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Header Authorization không hợp lệ: {}", authHeader);
                throw new InvalidCredentialsException("Token không hợp lệ hoặc thiếu");
            }
            String token = authHeader.substring(7);
            String username = jwtProvider.getUsernameFromToken(token);
            if (username == null) {
                logger.warn("Không thể trích xuất username từ token: {}", token);
                throw new InvalidCredentialsException("Token không hợp lệ");
            }
            logger.debug("Trích xuất username từ token thành công: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Lỗi khi trích xuất username từ token: {}", e.getMessage());
            throw new InvalidCredentialsException("Token không hợp lệ: " + e.getMessage());
        }
    }
}