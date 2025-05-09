package com.example.movie_streaming.movieService.controller;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.movieService.model.dto.response.MovieTrailerResponse;
import com.example.movie_streaming.movieService.model.dto.response.MovieViewResponse;
import com.example.movie_streaming.movieService.service.MovieViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie-views")
@RequiredArgsConstructor
public class MovieViewController {

    private final MovieViewService viewService;

    @GetMapping("/by-movie/{movieId}")
    public ResponseEntity<ApiResponse<List<MovieViewResponse>>> getByMovieId(@PathVariable("movieId") Long movieId) {
        try {
            List<MovieViewResponse> views = viewService.getViewsByMovieId(movieId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched movie views successfully", views));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch movie views: " + e.getMessage(), null));
        }
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ApiResponse<List<MovieViewResponse>>> getByUserId(@PathVariable("userId") Long userId) {
        try {
            List<MovieViewResponse> views = viewService.getViewsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched movie views successfully", views));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch movie views: " + e.getMessage(), null));
        }
    }
}
