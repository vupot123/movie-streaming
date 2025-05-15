package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.response.MovieResponse;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import com.example.movie_streaming.common.exceptions.*;
import org.springframework.stereotype.Service;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieRequest;
import com.example.movie_streaming.movieService.model.entity.MovieType;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        return toDto(movie);
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MovieResponse createMovie(CreateMovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .type(MovieType.valueOf(request.getType()))
                .year(request.getYear())
                .duration(request.getDuration())
                .intro(request.getIntro())
                .ageRating(request.getAgeRating())
                .views(request.getViews() != null ? request.getViews() : 0L)
                .build();

        try {
            // Chuyển movie thành JSON string
            String movieJson = objectMapper.writeValueAsString(movie);
            // Gửi JSON string qua Kafka
            kafkaProducerService.sendMessage("movie-topic", "CREATE_MOVIE:" + movieJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send movie to Kafka: " + e.getMessage());
        }

        return toDto(movie);
    }

    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        movie.setTitle(request.getTitle());
        movie.setType(MovieType.valueOf(request.getType()));
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());
        movie.setIntro(request.getIntro());
        movie.setAgeRating(request.getAgeRating());
        movie.setViews(request.getViews());

        try {
            // Chuyển movie thành JSON string
            String movieJson = objectMapper.writeValueAsString(movie);
            // Gửi JSON string qua Kafka
            kafkaProducerService.sendMessage("movie-topic", "UPDATE_MOVIE:" + movieJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send movie update to Kafka: " + e.getMessage());
        }

        return toDto(movie);
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found");
        }
        kafkaProducerService.sendMessage("movie-topic", "DELETE_MOVIE:" + id);
    }

    public void addView(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));
        movie.setViews(movie.getViews() + 1);

        try {
            // Chuyển movie thành JSON string
            String movieJson = objectMapper.writeValueAsString(movie);
            // Gửi JSON string qua Kafka
            kafkaProducerService.sendMessage("movie-topic", "VIEW_MOVIE:" + movieJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send view update to Kafka: " + e.getMessage());
        }
    }

    private MovieResponse toDto(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle() != null ? movie.getTitle() : "Unknown")
                .type(movie.getType() != null ? movie.getType().name() : "le")
                .year(movie.getYear() != null ? movie.getYear() : 2000)
                .duration(movie.getDuration() != null ? movie.getDuration() : 0)
                .intro(movie.getIntro() != null ? movie.getIntro() : "")
                .ageRating(movie.getAgeRating() != null ? movie.getAgeRating() : "N/A")
                .views(movie.getViews() != null ? movie.getViews() : 0L)
                .build();
    }
}