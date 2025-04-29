package com.example.movie_streaming.movieService.controller;

import com.example.movie_streaming.movieService.model.dto.MovieDto;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // API để lấy tất cả phim (trả về danh sách MovieDto)
    @GetMapping
    public List<MovieDto> getAllMovies() {
        return movieService.getAllMovies();
    }

    // API để lấy thông tin một bộ phim theo ID (trả về MovieDto)
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    // API để thêm một bộ phim mới (dùng MovieDto)
    @PostMapping
    public ResponseEntity<MovieDto> addMovie(@RequestBody MovieDto movieDto) {
        return ResponseEntity.ok(movieService.addMovie(movieDto));
    }

    // API để cập nhật thông tin phim (dùng MovieDto)
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Long id, @RequestBody MovieDto movieDto) {
        return ResponseEntity.ok(movieService.updateMovie(id, movieDto));
    }

    // API để tăng lượt xem cho một bộ phim
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> addView(@PathVariable Long id) {
        movieService.addView(id);
        return ResponseEntity.ok().build();
    }
}
