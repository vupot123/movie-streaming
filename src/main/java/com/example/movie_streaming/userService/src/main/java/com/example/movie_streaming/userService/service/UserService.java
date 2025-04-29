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
import com.example.movie_streaming.userService.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import com.example.movie_streaming.common.exceptions.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MovieClient movieClient; // 👈 thêm vào

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

        // 🛠 Gọi movieService để lấy movie title
        MovieResponse movie = movieClient.getMovieById(request.getMovieId());

        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .movieId(movie.getId())
                .movieTitle(movie.getTitle()) // 👈 lấy title từ MovieService
                .build();

        favoriteRepository.save(favorite);
    }

    public List<Favorite> getFavorites(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favoriteRepository.findByUserId(user.getId());
    }
}
