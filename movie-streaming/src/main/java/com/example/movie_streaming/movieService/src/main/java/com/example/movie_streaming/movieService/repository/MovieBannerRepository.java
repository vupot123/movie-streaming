package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieBannerRepository extends JpaRepository<MovieBanner, Long> {
    List<MovieBanner> findByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM MovieBanner mb WHERE mb.movie.id = :movieId")
    void deleteByMovieId(Long movieId);
}