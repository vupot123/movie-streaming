package com.example.movie_streaming.userService.service;

import com.example.movie_streaming.userService.client.MovieClient;
import com.example.movie_streaming.userService.kafka.KafkaMessage;
import com.example.movie_streaming.userService.kafka.KafkaProducerService;
import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
import com.example.movie_streaming.userService.model.dto.request.UpdateUserRequest;
import com.example.movie_streaming.userService.model.dto.response.JwtResponse;
import com.example.movie_streaming.userService.model.dto.response.MovieResponse;
import com.example.movie_streaming.userService.model.entity.Favorite;
import com.example.movie_streaming.userService.model.entity.MovieView;
import com.example.movie_streaming.userService.model.entity.User;
import com.example.movie_streaming.userService.repository.FavoriteRepository;
import com.example.movie_streaming.userService.repository.MovieViewRepository;
import com.example.movie_streaming.userService.repository.UserRepository;
import com.example.movie_streaming.common.security.JwtProvider;
import com.example.movie_streaming.common.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final MovieViewRepository movieViewRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MovieClient movieClient;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder; // Đảm bảo bean này được tiêm

    public void register(@Valid RegisterRequest request) {
        logger.debug("Bắt đầu đăng ký người dùng: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Username '{}' đã tồn tại", request.getUsername());
            throw new DuplicateResourceException("Username đã được sử dụng");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Email '{}' đã tồn tại", request.getEmail());
            throw new DuplicateResourceException("Email đã được sử dụng");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(1)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        logger.info("Đăng ký thành công cho người dùng: {}", user.getUsername());

        Map<String, Object> payload = Map.of("username", user.getUsername(), "email", user.getEmail());
        KafkaMessage kafkaMessage = new KafkaMessage("user", "REGISTER", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("user-registration", messageJson);
            logger.debug("Gửi tin nhắn Kafka thành công cho đăng ký: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho người dùng '{}': {}", user.getUsername(), e.getMessage());
        }
    }

    public JwtResponse login(@Valid LoginRequest request) {
        logger.debug("Bắt đầu đăng nhập cho người dùng: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", request.getUsername());
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Thông tin đăng nhập không hợp lệ cho người dùng: {}", request.getUsername());
            throw new InvalidCredentialsException("Mật khẩu không đúng");
        }

        String role = user.getRole() == 0 ? "ADMIN" : "USER";
        logger.debug("Xác định vai trò '{}' cho người dùng: {}", role, request.getUsername());

        String token = jwtProvider.generateToken(user.getUsername(), role);
        logger.info("Đăng nhập thành công cho người dùng: {}, vai trò: {}", user.getUsername(), role);
        return new JwtResponse(role, token);
    }

    public void addFavorite(String username, @Valid FavoriteRequest request) {
        logger.debug("Thêm phim yêu thích cho người dùng: {}, movieId: {}", username, request.getMovieId());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        MovieResponse movie = movieClient.getMovieById(request.getMovieId());
        if (movie == null) {
            logger.warn("Không tìm thấy phim với ID: {} từ movie-service", request.getMovieId());
            throw new ResourceNotFoundException("Phim không tồn tại");
        }

        if (favoriteRepository.findByUserAndMovieId(user, request.getMovieId()).isPresent()) {
            logger.warn("Phim với ID {} đã tồn tại trong danh sách yêu thích của: {}", request.getMovieId(), username);
            throw new DuplicateResourceException("Phim đã được thêm vào danh sách yêu thích");
        }

        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .user(user)
                .movieId(movie.getId())
                .createdAt(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);
        logger.info("Thêm phim yêu thích thành công cho người dùng: {}, movieId: {}", username, movie.getId());

        Map<String, Object> payload = Map.of("userId", user.getId(), "movieId", movie.getId());
        KafkaMessage kafkaMessage = new KafkaMessage("favorite", "ADD", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("favorite-events", messageJson);
            logger.debug("Gửi tin nhắn Kafka thành công cho sự kiện thêm yêu thích: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho sự kiện yêu thích: user={}, movieId={}", username, movie.getId(), e);
        }
    }

    public void removeFavorite(String username, Long movieId) {
        logger.debug("Xóa phim yêu thích cho người dùng: {}, movieId: {}", username, movieId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        Favorite favorite = favoriteRepository.findByUserAndMovieId(user, movieId)
                .orElseThrow(() -> {
                    logger.warn("Phim với ID {} không tồn tại trong danh sách yêu thích của: {}", movieId, username);
                    return new ResourceNotFoundException("Phim không có trong danh sách yêu thích");
                });

        favoriteRepository.delete(favorite);
        logger.info("Xóa phim yêu thích thành công cho người dùng: {}, movieId: {}", username, movieId);

        Map<String, Object> payload = Map.of("userId", user.getId(), "movieId", movieId);
        KafkaMessage kafkaMessage = new KafkaMessage("favorite", "REMOVE", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("favorite-events", messageJson);
            logger.debug("Gửi tin nhắn Kafka thành công cho sự kiện xóa yêu thích: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho sự kiện xóa yêu thích: user={}, movieId={}", username, movieId, e);
        }
    }

    public Page<Map<String, Object>> getFavorites(String username, Pageable pageable) {
        logger.debug("Lấy danh sách phim yêu thích cho người dùng: {} với phân trang", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        Page<Favorite> favorites = favoriteRepository.findByUser(user, pageable);
        logger.info("Đã lấy được {} phim yêu thích cho người dùng: {} từ database", favorites.getTotalElements(), username);

        return favorites.map(favorite -> {
            Long movieId = favorite.getMovieId();
            MovieResponse movie = movieClient.getMovieById(movieId);
            Map<String, Object> favoriteMap = new HashMap<>();
            favoriteMap.put("movieId", movieId);
            favoriteMap.put("title", (movie != null) ? movie.getTitle() : "Phim không tìm thấy");
            favoriteMap.put("createdAt", favorite.getCreatedAt());
            return favoriteMap;
        });
    }

    public Map<String, Object> getUserDetail(String username, Pageable pageable) {
        logger.debug("Lấy thông tin chi tiết người dùng: {} với phân trang", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole() == 0 ? "ADMIN" : "USER");
        userInfo.put("name", user.getName());
        userInfo.put("createdAt", user.getCreatedAt());

        Page<Favorite> favorites = favoriteRepository.findByUser(user, pageable);
        Page<Map<String, Object>> favoritesPage = favorites.map(favorite -> {
            Long movieId = favorite.getMovieId();
            MovieResponse movie = movieClient.getMovieById(movieId);
            Map<String, Object> favoriteMap = new HashMap<>();
            favoriteMap.put("movieId", movieId);
            favoriteMap.put("title", (movie != null) ? movie.getTitle() : "Phim không tìm thấy");
            favoriteMap.put("createdAt", favorite.getCreatedAt());
            return favoriteMap;
        });
        userInfo.put("favorites", favoritesPage);

        logger.info("Lấy thông tin chi tiết thành công cho người dùng: {}", username);
        return userInfo;
    }

    public void updateUser(String username, UpdateUserRequest updateRequest) {
        logger.debug("Cập nhật thông tin người dùng: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        if (!user.getEmail().equals(updateRequest.getEmail())) {
            if (userRepository.findByEmail(updateRequest.getEmail()).isPresent()) {
                logger.warn("Email '{}' đã tồn tại", updateRequest.getEmail());
                throw new DuplicateResourceException("Email đã được sử dụng");
            }
        }

        // Cập nhật password nếu được cung cấp và không rỗng
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        user.setEmail(updateRequest.getEmail());
        user.setName(updateRequest.getName());
        userRepository.save(user);
        logger.info("Cập nhật thông tin thành công cho người dùng: {}", username);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("name", user.getName());
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            payload.put("passwordUpdated", true); // Không gửi password thô
        }
        KafkaMessage kafkaMessage = new KafkaMessage("user", "UPDATE", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("user-events", messageJson);
            logger.debug("Gửi tin nhắn Kafka thành công cho sự kiện cập nhật: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho người dùng '{}': {}", username, e.getMessage());
        }
    }

    public void recordMovieView(String username, Long movieId) {
        logger.debug("Ghi lại lịch sử xem phim cho người dùng: {}, movieId: {}", username, movieId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        MovieResponse movie = movieClient.getMovieById(movieId);
        if (movie == null) {
            logger.warn("Không tìm thấy phim với ID: {} từ movie-service", movieId);
            throw new ResourceNotFoundException("Phim không tồn tại");
        }

        MovieView movieView = MovieView.builder()
                .user(user)
                .movieId(movieId)
                .viewedAt(LocalDateTime.now())
                .build();
        movieViewRepository.save(movieView);
        logger.info("Ghi lại lượt xem phim thành công cho người dùng: {}, movieId: {}", username, movieId);

        Map<String, Object> payload = Map.of("userId", user.getId(), "movieId", movieId);
        KafkaMessage kafkaMessage = new KafkaMessage("movie-view", "VIEW", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("movie-views", messageJson);
            logger.debug("Gửi tin nhắn Kafka thành công cho sự kiện xem phim: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho sự kiện xem phim: user={}, movieId={}", username, movieId, e);
        }
    }

    public Map<String, Object> getMe(String username) {
        logger.debug("Lấy thông tin người dùng hiện tại: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Người dùng không tồn tại");
                });

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole() == 0 ? "ADMIN" : "USER");
        userInfo.put("name", user.getName());
        userInfo.put("createdAt", user.getCreatedAt());

        logger.info("Lấy thông tin thành công cho người dùng: {}", username);
        return userInfo;
    }
}