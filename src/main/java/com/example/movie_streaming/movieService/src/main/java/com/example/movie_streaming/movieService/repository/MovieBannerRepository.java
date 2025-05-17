package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieBannerRepository extends JpaRepository<MovieBanner, Long> {
    List<MovieBanner> findByMovieId(Long movieId);
}
