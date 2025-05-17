package com.example.movie_streaming.movieService.controller;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieTrailerRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieTrailerRequest;
import com.example.movie_streaming.movieService.model.dto.response.MovieTrailerResponse;
import com.example.movie_streaming.movieService.service.MovieTrailerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies/{movieId}/trailers")
@RequiredArgsConstructor
public class MovieTrailerController {

    private final MovieTrailerService trailerService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTrailer(
            @PathVariable("movieId") Long movieId,
            @RequestBody @Valid CreateMovieTrailerRequest request
    ) {
        try {
            MovieTrailerResponse trailer = trailerService.create(movieId, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Created trailer successfully", trailer));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to create trailer: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTrailers(@PathVariable("movieId") Long movieId) {
        try {
            List<MovieTrailerResponse> trailers = trailerService.getByMovieId(movieId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched trailers successfully", trailers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch trailers: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getTrailerById(@PathVariable("id") Long id) {
        try {
            MovieTrailerResponse trailer = trailerService.getById(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched trailer successfully", trailer));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch trailer: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTrailer(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMovieTrailerRequest request
    ) {
        try {
            MovieTrailerResponse updated = trailerService.update(id, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Updated trailer successfully", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to update trailer: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteTrailer(@PathVariable("id") Long id) {
        try {
            trailerService.delete(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Deleted trailer successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to delete trailer: " + e.getMessage(), null));
        }
    }
}
