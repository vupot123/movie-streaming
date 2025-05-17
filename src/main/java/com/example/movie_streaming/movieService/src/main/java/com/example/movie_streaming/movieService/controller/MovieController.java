package com.example.movie_streaming.movieService.controller;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.request.MovieFilterRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.response.MovieResponse;
import com.example.movie_streaming.movieService.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createMovie(@RequestBody CreateMovieRequest request) {
        try {
            MovieResponse createdMovie = movieService.createMovie(request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Movie created successfully", createdMovie));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to create movie: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getMovieById(@PathVariable("id") Long id) {
        try {
            MovieResponse movie = movieService.getMovieById(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Movie fetched successfully", movie));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch movie: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllMovies() {
        try {
            List<MovieResponse> movies = movieService.getAllMovies();
            return ResponseEntity.ok(new ApiResponse<>(200, "Movies fetched successfully", movies));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch movies: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateMovie(@PathVariable("id") Long id,
                                                      @RequestBody UpdateMovieRequest request) {
        try {
            MovieResponse updatedMovie = movieService.updateMovie(id, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Movie updated successfully", updatedMovie));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to update movie: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteMovie(@PathVariable("id") Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Movie deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to delete movie: " + e.getMessage(), null));
        }
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<ApiResponse<?>> addView(@PathVariable("id") Long id) {
        try {
            movieService.addView(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "View added successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to add view: " + e.getMessage(), null));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> filterMovies(MovieFilterRequest request) {
        try {
            Page<MovieResponse> result = movieService.filterMovies(request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Filtered movies successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to filter movies: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchMovies(@RequestParam("keyword") String keyword) {
        try {
            List<MovieResponse> result = movieService.searchMovies(keyword);
            return ResponseEntity.ok(new ApiResponse<>(200, "Search success", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Search failed: " + e.getMessage(), null));
        }
    }

}
