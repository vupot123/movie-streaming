package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieTrailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieTrailerRepository extends JpaRepository<MovieTrailer, Long> {
    List<MovieTrailer> findByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM MovieTrailer mt WHERE mt.movie.id = :movieId")
    void deleteByMovieId(Long movieId);
}