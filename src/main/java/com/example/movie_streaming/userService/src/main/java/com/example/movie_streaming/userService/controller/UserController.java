package com.example.movie_streaming.userService.controller;

import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.userService.model.dto.request.UpdateUserRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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

    @DeleteMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(HttpServletRequest request, @PathVariable Long movieId) {
        try {
            logger.debug("Yêu cầu xóa phim yêu thích với movieId: {}", movieId);
            String username = extractUsernameFromRequest(request);
            userService.removeFavorite(username, movieId);
            logger.info("Xóa phim yêu thích thành công cho username: {}, movieId: {}", username, movieId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa phim yêu thích thành công", null));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi xóa phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi xóa phim yêu thích: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi xóa phim yêu thích với movieId: {}", movieId, e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getFavorites(
            HttpServletRequest request,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        try {
            logger.debug("Yêu cầu lấy danh sách phim yêu thích với phân trang");
            String username = extractUsernameFromRequest(request);
            Page<Map<String, Object>> favorites = userService.getFavorites(username, pageable);
            logger.info("Lấy danh sách phim yêu thích thành công cho username: {}, trang: {}, kích thước: {}",
                    username, pageable.getPageNumber(), pageable.getPageSize());
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

    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetail(
            HttpServletRequest request,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        try {
            logger.debug("Yêu cầu lấy thông tin chi tiết người dùng với phân trang");
            String username = extractUsernameFromRequest(request);
            Map<String, Object> userDetail = userService.getUserDetail(username, pageable);
            logger.info("Lấy thông tin chi tiết thành công cho username: {}", username);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin chi tiết người dùng thành công", userDetail));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi lấy thông tin chi tiết: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi lấy thông tin chi tiết: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi lấy thông tin chi tiết", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateUser(HttpServletRequest request, @Valid @RequestBody UpdateUserRequest updateRequest) {
        try {
            logger.debug("Yêu cầu cập nhật thông tin người dùng");
            String username = extractUsernameFromRequest(request);
            userService.updateUser(username, updateRequest);
            logger.info("Cập nhật thông tin thành công cho username: {}", username);
            return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật thông tin người dùng thành công", null));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi cập nhật thông tin: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi cập nhật thông tin: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (DuplicateResourceException e) {
            logger.warn("Lỗi cập nhật thông tin: Email đã tồn tại", e.getMessage());
            return ResponseEntity.status(409).body(new ApiResponse<>(409, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi cập nhật thông tin người dùng", e);
            return ResponseEntity.status(500).body(new ApiResponse<>(500, "Lỗi hệ thống", null));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        try {
            logger.debug("Yêu cầu thay đổi mật khẩu cho người dùng");
            String username = extractUsernameFromRequest(request);
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                logger.warn("Thiếu oldPassword hoặc newPassword trong yêu cầu");
                throw new IllegalArgumentException("Cả oldPassword và newPassword là bắt buộc");
            }

            userService.changePassword(username, oldPassword, newPassword);
            logger.info("Thay đổi mật khẩu thành công cho username: {}", username);
            return ResponseEntity.ok(new ApiResponse<>(200, "Thay đổi mật khẩu thành công", null));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi thay đổi mật khẩu: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi thay đổi mật khẩu: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            logger.warn("Lỗi thay đổi mật khẩu: {}", e.getMessage());
            return ResponseEntity.status(400).body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi thay đổi mật khẩu", e);
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMe(HttpServletRequest request) {
        try {
            logger.debug("Yêu cầu lấy thông tin người dùng hiện tại");
            String username = extractUsernameFromRequest(request);
            Map<String, Object> userInfo = userService.getMe(username);
            logger.info("Lấy thông tin người dùng thành công cho username: {}", username);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin người dùng thành công", userInfo));
        } catch (InvalidCredentialsException e) {
            logger.warn("Lỗi lấy thông tin người dùng: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ApiResponse<>(401, e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            logger.warn("Lỗi lấy thông tin người dùng: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi lấy thông tin người dùng", e);
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