package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.movieService.kafka.KafkaMessage;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieTrailerRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieTrailerRequest;
import com.example.movie_streaming.movieService.model.dto.response.MovieTrailerResponse;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.model.entity.MovieTrailer;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import com.example.movie_streaming.movieService.repository.MovieTrailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieTrailerService {

    private final MovieTrailerRepository trailerRepository;
    private final MovieRepository movieRepository;
    private final KafkaProducerService kafkaProducerService;


    public MovieTrailerResponse create(Long movieId, CreateMovieTrailerRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        MovieTrailer trailer = new MovieTrailer();
        trailer.setMovie(movie);
        trailer.setUrl(request.getUrl());

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage(
                "banner", "CREATE", trailer.getId(),
                Map.of("movieId", movieId, "trailerUrl", trailer.getUrl())
        ));


        trailer = trailerRepository.save(trailer);
        return toResponse(trailer);
    }

    public MovieTrailerResponse getById(Long id) {
        MovieTrailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + id));
        return toResponse(trailer);
    }

    public List<MovieTrailerResponse> getByMovieId(Long movieId) {
        return trailerRepository.findByMovieId(movieId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MovieTrailerResponse update(Long id, UpdateMovieTrailerRequest request) {
        MovieTrailer trailer = trailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trailer not found with id: " + id));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));

        trailer.setMovie(movie);
        trailer.setUrl(request.getUrl());

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage(
                "banner", "CREATE", trailer.getId(),
                Map.of("movieId", movie.getId(), "trailerUrl", trailer.getUrl())
        ));

        return toResponse(trailerRepository.save(trailer));
    }

    public void delete(Long id) {
        if (!trailerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trailer not found with id: " + id);
        }
        trailerRepository.deleteById(id);
    }

    private MovieTrailerResponse toResponse(MovieTrailer trailer) {
        return new MovieTrailerResponse(
                trailer.getId(),
                trailer.getMovie().getId(),
                trailer.getUrl()
        );
    }
}
