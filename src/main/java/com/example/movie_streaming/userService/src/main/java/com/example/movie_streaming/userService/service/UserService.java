package com.example.movie_streaming.userService.service;

import com.example.movie_streaming.userService.client.MovieClient;
import com.example.movie_streaming.userService.kafka.KafkaMessage;
import com.example.movie_streaming.userService.kafka.KafkaProducerService;
import com.example.movie_streaming.userService.model.dto.request.FavoriteRequest;
import com.example.movie_streaming.userService.model.dto.request.LoginRequest;
import com.example.movie_streaming.userService.model.dto.request.RegisterRequest;
import com.example.movie_streaming.userService.model.dto.response.MovieResponse;
import com.example.movie_streaming.userService.model.entity.Favorite;
import com.example.movie_streaming.userService.model.entity.User;
import com.example.movie_streaming.userService.repository.FavoriteRepository;
import com.example.movie_streaming.userService.repository.UserRepository;
import com.example.movie_streaming.common.security.JwtProvider;
import com.example.movie_streaming.common.exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MovieClient movieClient;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper; // Thêm ObjectMapper để serialize KafkaMessage

    public void register(RegisterRequest request) {
        logger.debug("Registering user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(new BCryptPasswordEncoder().encode(request.getPassword()))
                .name(request.getName())
                .build();
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        // Tạo KafkaMessage với payload chứa username và email
        Map<String, Object> payload = Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        );
        KafkaMessage kafkaMessage = new KafkaMessage("user", "REGISTER", user.getId(), payload);

        // Serialize KafkaMessage thành JSON
        try {
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            kafkaProducerService.sendMessage("user-registration", messageJson);
            logger.debug("Sent Kafka message for user registration: {}", messageJson);
        } catch (Exception e) {
            logger.error("Error serializing Kafka message for user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }

    public String login(LoginRequest request) {
        logger.debug("Logging in user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!new BCryptPasswordEncoder().matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(user.getUsername());
        logger.info("User logged in successfully: {}", user.getUsername());
        return token;
    }

    public void addFavorite(String username, FavoriteRequest request) {
        logger.debug("Adding favorite for user: {}, movieId: {}", username, request.getMovieId());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MovieResponse movie = movieClient.getMovieById(request.getMovieId());

        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .build();

        favoriteRepository.save(favorite);
        logger.info("Favorite added for user: {}, movieId: {}", username, movie.getId());
    }

    public List<Favorite> getFavorites(String username) {
        logger.debug("Getting favorites for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Favorite> favorites = favoriteRepository.findByUserId(user.getId());
        logger.info("Retrieved {} favorites for user: {}", favorites.size(), username);
        return favorites;
    }
}