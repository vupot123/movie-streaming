package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieActorRepository extends JpaRepository<MovieActor, Long> {
    List<MovieActor> findByMovieId(Long movieId);
}
