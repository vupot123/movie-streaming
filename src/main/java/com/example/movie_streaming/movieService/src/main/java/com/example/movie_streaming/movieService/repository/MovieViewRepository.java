package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieViewRepository extends JpaRepository<MovieView, Long> {
    List<MovieView> findByMovieId(Long movieId);
    List<MovieView> findByUserId(Long userId);
}
