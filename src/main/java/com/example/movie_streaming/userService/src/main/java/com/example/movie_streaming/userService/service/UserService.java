package com.example.movie_streaming.userService.service;

import com.example.movie_streaming.userService.client.MovieClient;
import com.example.movie_streaming.userService.kafka.KafkaMessage;
import com.example.movie_streaming.userService.kafka.KafkaProducerService;
import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public void register(@Valid RegisterRequest request) {
        logger.debug("Đang đăng ký người dùng: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.warn("Username {} đã tồn tại", request.getUsername());
            throw new DuplicateResourceException("Username đã tồn tại");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Email {} đã tồn tại", request.getEmail());
            throw new DuplicateResourceException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(new BCryptPasswordEncoder().encode(request.getPassword()))
                .name(request.getName())
                .role(1)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        logger.info("Đăng ký người dùng thành công: {}", user.getUsername());

        Map<String, Object> payload = Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        );
        KafkaMessage kafkaMessage = new KafkaMessage("user", "REGISTER", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("user-registration", messageJson);
            logger.debug("Đã gửi tin nhắn Kafka cho đăng ký người dùng: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho người dùng: {}", user.getUsername(), e);
        }
    }

    public JwtResponse login(@Valid LoginRequest request) {
        logger.debug("Đang đăng nhập người dùng: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", request.getUsername());
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        if (!new BCryptPasswordEncoder().matches(request.getPassword(), user.getPassword())) {
            logger.warn("Thông tin đăng nhập không hợp lệ cho người dùng: {}", request.getUsername());
            throw new InvalidCredentialsException("Thông tin đăng nhập không hợp lệ");
        }

        String role;
        if (user.getRole() == 0) {
            role = "ADMIN";
            logger.debug("Người dùng {} được xác định là ADMIN", request.getUsername());
        } else if (user.getRole() == 1) {
            role = "USER";
            logger.debug("Người dùng {} được xác định là USER", request.getUsername());
        } else {
            logger.warn("Vai trò không hợp lệ cho người dùng: {}, sử dụng USER làm mặc định", request.getUsername());
            role = "USER";
        }

        String token = jwtProvider.generateToken(user.getUsername(), role);
        logger.info("Đăng nhập thành công cho người dùng: {}, vai trò: {}", user.getUsername(), role);
        return new JwtResponse(role, token);
    }

    public void addFavorite(String username, @Valid FavoriteRequest request) {
        logger.debug("Thêm phim yêu thích cho người dùng: {}, movieId: {}", username, request.getMovieId());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        MovieResponse movie = movieClient.getMovieById(request.getMovieId());
        if (movie == null) {
            logger.warn("Không tìm thấy phim với ID: {} từ movie-service", request.getMovieId());
            throw new ResourceNotFoundException("Không tìm thấy phim");
        }

        if (favoriteRepository.findByUserAndMovieId(user, request.getMovieId()).isPresent()) {
            logger.warn("Phim với ID {} đã có trong danh sách yêu thích của người dùng: {}", request.getMovieId(), username);
            throw new DuplicateResourceException("Phim đã có trong danh sách yêu thích");
        }

        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .user(user)
                .movieId(movie.getId())
                .createdAt(LocalDateTime.now())
                .build();

        favoriteRepository.save(favorite);
        logger.info("Đã thêm phim yêu thích cho người dùng: {}, movieId: {}", username, movie.getId());

        Map<String, Object> payload = Map.of(
                "userId", user.getId(),
                "movieId", movie.getId()
        );
        KafkaMessage kafkaMessage = new KafkaMessage("favorite", "ADD", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("favorite-events", messageJson);
            logger.debug("Đã gửi tin nhắn Kafka cho sự kiện thêm yêu thích: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho sự kiện yêu thích: user={}, movieId={}", username, movie.getId(), e);
        }
    }

    public void removeFavorite(String username, Long movieId) {
        logger.debug("Xóa phim yêu thích cho người dùng: {}, movieId: {}", username, movieId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        Favorite favorite = favoriteRepository.findByUserAndMovieId(user, movieId)
                .orElseThrow(() -> {
                    logger.warn("Phim với ID {} không có trong danh sách yêu thích của người dùng: {}", movieId, username);
                    return new ResourceNotFoundException("Phim không có trong danh sách yêu thích");
                });

        favoriteRepository.delete(favorite);
        logger.info("Đã xóa phim yêu thích cho người dùng: {}, movieId: {}", username, movieId);

        Map<String, Object> payload = Map.of(
                "userId", user.getId(),
                "movieId", movieId
        );
        KafkaMessage kafkaMessage = new KafkaMessage("favorite", "REMOVE", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("favorite-events", messageJson);
            logger.debug("Đã gửi tin nhắn Kafka cho sự kiện xóa yêu thích: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho sự kiện xóa yêu thích: user={}, movieId={}", username, movieId, e);
        }
    }

    public List<Map<String, Object>> getFavorites(String username) {
        logger.debug("Lấy danh sách phim yêu thích cho người dùng: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        logger.info("Đã lấy được {} phim yêu thích cho người dùng: {} từ database", favorites.size(), username);

        if (favorites.isEmpty()) {
            logger.warn("Không có phim yêu thích nào cho người dùng: {}", username);
            return List.of(); // Trả về danh sách rỗng thay vì null
        }

        return favorites.stream().map(favorite -> {
            Long movieId = favorite.getMovieId();
            MovieResponse movie = movieClient.getMovieById(movieId);
            if (movie == null) {
                logger.warn("Không tìm thấy thông tin phim cho movieId: {} từ movie-service", movieId);
            }
            Map<String, Object> favoriteMap = new HashMap<>();
            favoriteMap.put("movieId", movieId);
            favoriteMap.put("title", (movie != null) ? movie.getTitle() : "Không tìm thấy phim");
            favoriteMap.put("createdAt", favorite.getCreatedAt());
            return favoriteMap;
        }).collect(Collectors.toList());
    }

    public void recordMovieView(String username, Long movieId) {
        logger.debug("Ghi lại lịch sử xem phim cho người dùng: {}, movieId: {}", username, movieId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy người dùng: {}", username);
                    return new ResourceNotFoundException("Không tìm thấy người dùng");
                });

        MovieResponse movie = movieClient.getMovieById(movieId);
        if (movie == null) {
            logger.warn("Không tìm thấy phim với ID: {} từ movie-service", movieId);
            throw new ResourceNotFoundException("Không tìm thấy phim");
        }

        MovieView movieView = MovieView.builder()
                .user(user)
                .movieId(movieId)
                .viewedAt(LocalDateTime.now())
                .build();

        movieViewRepository.save(movieView);
        logger.info("Đã ghi lại lịch sử xem phim cho người dùng: {}, movieId: {}", username, movieId);

        Map<String, Object> payload = Map.of(
                "userId", user.getId(),
                "movieId", movieId
        );
        KafkaMessage kafkaMessage = new KafkaMessage("movie-view", "VIEW", user.getId(), payload);

        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("movie-views", messageJson);
            logger.debug("Đã gửi tin nhắn Kafka cho sự kiện xem phim: {}", messageJson);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn Kafka cho sự kiện xem phim: user={}, movieId={}", username, movieId, e);
        }
    }
}