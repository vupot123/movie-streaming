package com.example.movie_streaming.movieService.controller;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.common.response.ApiResponse;
import com.example.movie_streaming.movieService.model.dto.request.CreateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.request.MovieFilterRequest;
import com.example.movie_streaming.movieService.model.dto.request.UpdateMovieRequest;
import com.example.movie_streaming.movieService.model.dto.response.MovieResponse;
import com.example.movie_streaming.movieService.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@RequestBody CreateMovieRequest request) {
        log.info("Nhận yêu cầu tạo phim mới: {}", request.getTitle());
        MovieResponse createdMovie = movieService.createMovie(request);
        log.info("Tạo phim thành công với ID: {}", createdMovie.getId());
        return ResponseEntity.ok(ApiResponse.success(200, "Movie created successfully", createdMovie));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable("id") Long id) {
        log.info("Nhận yêu cầu lấy phim với ID: {}", id);
        MovieResponse movie = movieService.getMovieById(id);
        log.info("Lấy phim thành công với ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(200, "Movie fetched successfully", movie));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getAllMovies() {
        log.info("Nhận yêu cầu lấy tất cả phim");
        List<MovieResponse> movies = movieService.getAllMovies();
        log.info("Lấy thành công {} phim", movies.size());
        return ResponseEntity.ok(ApiResponse.success(200, "Movies fetched successfully", movies));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(@PathVariable("id") Long id,
                                                                  @RequestBody UpdateMovieRequest request) {
        log.info("Nhận yêu cầu cập nhật phim với ID: {}", id);
        MovieResponse updatedMovie = movieService.updateMovie(id, request);
        log.info("Cập nhật phim thành công với ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(200, "Movie updated successfully", updatedMovie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable("id") Long id) {
        log.info("Nhận yêu cầu xóa phim với ID: {}", id);
        movieService.deleteMovie(id);
        log.info("Xóa phim thành công với ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(200, "Movie deleted successfully", null));
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<ApiResponse<Void>> addView(@PathVariable("id") Long id) {
        log.info("Nhận yêu cầu tăng lượt xem cho phim với ID: {}", id);
        movieService.addView(id);
        log.info("Tăng lượt xem thành công cho phim với ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(200, "View added successfully", null));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<MovieResponse>>> filterMovies(@RequestBody @Valid MovieFilterRequest request) {
        log.info("Nhận yêu cầu lọc phim với điều kiện: {}", request);
        Page<MovieResponse> result = movieService.filterMovies(request);
        log.info("Lọc phim thành công, số lượng: {}", result.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(200, "Filtered movies successfully", result));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> searchMovies(@RequestParam("keyword") String keyword) {
        log.info("Nhận yêu cầu tìm kiếm phim với từ khóa: {}", keyword);
        List<MovieResponse> result = movieService.searchMovies(keyword);
        log.info("Tìm kiếm phim thành công, số lượng: {}", result.size());
        return ResponseEntity.ok(ApiResponse.success(200, "Search success", result));
    }
}