package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.SingleMovieStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SingleMovieStreamRepository extends JpaRepository<SingleMovieStream, Long> {
    Optional<SingleMovieStream> findByMovieId(Long movieId);
}

