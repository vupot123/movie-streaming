package com.example.movie_streaming.movieService.controller;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieBannerRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieBannerRequest;
import com.example.movie_streaming.movieService.model.dto.response.MovieBannerResponse;
import com.example.movie_streaming.movieService.service.MovieBannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies/{movieId}/banners")
@RequiredArgsConstructor
public class MovieBannerController {

    private final MovieBannerService bannerService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createBanner(
            @PathVariable("movieId") Long movieId,
            @RequestBody @Valid CreateMovieBannerRequest request
    ) {
        try {
            MovieBannerResponse banner = bannerService.create(movieId, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Created banner successfully", banner));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to create banner: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBanners(@PathVariable("movieId") Long movieId) {
        try {
            List<MovieBannerResponse> banners = bannerService.getByMovieId(movieId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched banners successfully", banners));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to fetch banners: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getBannerById(@PathVariable("id") Long id) {
        try {
            MovieBannerResponse banner = bannerService.getById(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Fetched banner successfully", banner));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateBanner(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMovieBannerRequest request
    ) {
        try {
            MovieBannerResponse updated = bannerService.update(id, request);
            return ResponseEntity.ok(new ApiResponse<>(200, "Updated banner successfully", updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBanner(@PathVariable("id") Long id) {
        try {
            bannerService.delete(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Deleted banner successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }
}
