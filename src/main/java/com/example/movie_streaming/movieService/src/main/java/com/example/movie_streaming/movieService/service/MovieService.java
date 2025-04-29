package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.response.MovieResponse;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import com.example.movie_streaming.common.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
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
                .views(request.getViews())
                .build();

        Movie savedMovie = movieRepository.save(movie);

        // Kafka event
        kafkaProducerService.sendMessage("movie-topic", "New movie added: " + savedMovie.getTitle());

        return toDto(savedMovie);
    }


    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        movie.setId(id);
        movie.setTitle(request.getTitle());
        movie.setType(MovieType.valueOf(request.getType()));
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());
        movie.setIntro(request.getIntro());
        movie.setAgeRating(request.getAgeRating());
        movie.setViews(request.getViews());

        Movie updated = movieRepository.save(movie);
        kafkaProducerService.sendMessage("movie-topic", "Movie with ID " + id + " has been updated");

        return toDto(updated);
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found");
        }
        movieRepository.deleteById(id);
    }

    public void addView(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));
        movie.setViews(movie.getViews() + 1);
        movieRepository.save(movie);

        String message = "Movie with ID " + id + " has been viewed";
        kafkaProducerService.sendMessage("movie-topic", message);
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
