package com.example.movie_streaming.movieService.service;

<<<<<<< HEAD
import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.movieService.kafka.KafkaMessage;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
=======
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
>>>>>>> dev
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.request.MovieFilterRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.response.*;
import com.example.movie_streaming.movieService.model.entity.*;
import com.example.movie_streaming.movieService.repository.*;
import com.example.movie_streaming.movieService.specification.MovieSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

=======
>>>>>>> dev
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
<<<<<<< HEAD
    private final MovieTrailerRepository trailerRepository;
    private final MovieBannerRepository bannerRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MovieActorRepository movieActorRepository;
    private final SingleMovieStreamRepository singleMovieStreamRepository;

    public Page<MovieResponse> filterMovies(MovieFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() - 1 : 0,
                request.getSize() != null ? request.getSize() : 20
        );

        Page<Movie> movies = movieRepository.findAll(new MovieSpecification(request), pageable);
        return movies.map(this::toResponse);
    }
=======
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;
>>>>>>> dev

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));
        return toResponse(movie);
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MovieResponse createMovie(CreateMovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .type(MovieType.fromString(request.getType()))
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

<<<<<<< HEAD
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage(
                "movie", "CREATE", savedMovie.getId(),
                Map.of("title", savedMovie.getTitle())
        ));

        return toResponse(savedMovie);
=======
        return toDto(movie);
>>>>>>> dev
    }

    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        movie.setTitle(request.getTitle());
        movie.setType(MovieType.fromString(request.getType()));
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());
        movie.setIntro(request.getIntro());
        movie.setAgeRating(request.getAgeRating());
        movie.setViews(request.getViews());

<<<<<<< HEAD
        Movie updated = movieRepository.save(movie);

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage(
                "movie", "UPDATE", updated.getId(),
                Map.of("title", updated.getTitle())
        ));

        return toResponse(updated);
=======
        try {
            // Chuyển movie thành JSON string
            String movieJson = objectMapper.writeValueAsString(movie);
            // Gửi JSON string qua Kafka
            kafkaProducerService.sendMessage("movie-topic", "UPDATE_MOVIE:" + movieJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send movie update to Kafka: " + e.getMessage());
        }

        return toDto(movie);
>>>>>>> dev
    }

    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found");
        }
<<<<<<< HEAD

        movieRepository.deleteById(id);

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage(
                "movie", "DELETE", id,
                Map.of("deleted", true)
        ));
=======
        kafkaProducerService.sendMessage("movie-topic", "DELETE_MOVIE:" + id);
>>>>>>> dev
    }

    public void addView(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));
        movie.setViews(movie.getViews() + 1);

<<<<<<< HEAD
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage(
                "movie", "VIEW", id,
                Map.of("views", movie.getViews())
        ));
=======
        try {
            // Chuyển movie thành JSON string
            String movieJson = objectMapper.writeValueAsString(movie);
            // Gửi JSON string qua Kafka
            kafkaProducerService.sendMessage("movie-topic", "VIEW_MOVIE:" + movieJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send view update to Kafka: " + e.getMessage());
        }
>>>>>>> dev
    }

    public List<MovieResponse> searchMovies(String keyword) {
        List<Movie> movies = movieRepository.searchByTitleOrActorName(keyword);
        if (movies.isEmpty()) {
            throw new ResourceNotFoundException("No movies found with keyword: " + keyword);
        }

        return movies.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MovieResponse toResponse(Movie movie) {
        List<MovieTrailer> trailers = trailerRepository.findByMovieId(movie.getId());
        List<MovieBanner> banners = bannerRepository.findByMovieId(movie.getId());
        List<MovieActor> actors = movieActorRepository.findByMovieId(movie.getId());

        List<ActorResponse> actorResponses = actors.stream()
                .map(a -> new ActorResponse(a.getActor().getId(), a.getActor().getName()))
                .collect(Collectors.toList());

        SingleMovieStreamResponse streamResponse = singleMovieStreamRepository.findByMovieId(movie.getId())
                .map(stream -> new SingleMovieStreamResponse(
                        stream.getId(),
                        stream.getFileName(),
                        stream.getFileUrl()
                )).orElse(null);

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .type(movie.getType().toString())
                .year(movie.getYear())
                .duration(movie.getDuration())
                .intro(movie.getIntro())
                .ageRating(movie.getAgeRating())
                .views(movie.getViews())
                .trailers(trailers.stream().map(trailer -> new MovieTrailerResponse(
                        trailer.getId(),
                        trailer.getMovie().getId(),
                        trailer.getUrl()
                )).collect(Collectors.toList()))
                .banners(banners.stream().map(banner -> new MovieBannerResponse(
                        banner.getId(),
                        banner.getMovie().getId(),
                        banner.getSmallBanner(),
                        banner.getLargeBanner()
                )).collect(Collectors.toList()))
                .actors(actorResponses)
                .stream(streamResponse) // phải có .stream(SingleMovieStreamResponse)
                .build();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> dev
