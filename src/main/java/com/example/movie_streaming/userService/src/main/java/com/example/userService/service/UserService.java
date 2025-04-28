package com.example.userService.service;

import com.example.userService.kafka.KafkaMessage;
import com.example.userService.model.dto.request.FavoriteRequest;
import com.example.userService.model.dto.request.LoginRequest;
import com.example.userService.model.dto.request.RegisterRequest;
import com.example.userService.model.entity.*;
import com.example.userService.repository.*;
import com.example.userService.security.JwtProvider;
import com.example.userService.kafka.KafkaProducerService;
import com.example.userService.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final KafkaProducerService kafkaProducerService;

    public void register(RegisterRequest request) {
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

        // Gửi sự kiện Kafka
        KafkaMessage kafkaMessage = new KafkaMessage(user.getUsername(), user.getEmail(), "REGISTER");
        kafkaProducerService.sendMessage("user-registration", kafkaMessage);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!new BCryptPasswordEncoder().matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return JwtProvider.generateToken(user.getUsername());
    }

    public void addFavorite(String username, FavoriteRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .movieId(request.getMovieId())
                .movieTitle(request.getMovieTitle())
                .build();

        favoriteRepository.save(favorite);
    }

    public List<Favorite> getFavorites(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.findByUserId(user.getId());
    }
}
