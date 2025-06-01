package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieActor;
import com.example.movie_streaming.movieService.model.entity.MovieActorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieActorRepository extends JpaRepository<MovieActor, MovieActorId> {
    List<MovieActor> findByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM MovieActor ma WHERE ma.movie.id = :movieId")
    void deleteByMovieId(Long movieId);
}