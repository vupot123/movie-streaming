package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Movie findByTitle(String title);
}
