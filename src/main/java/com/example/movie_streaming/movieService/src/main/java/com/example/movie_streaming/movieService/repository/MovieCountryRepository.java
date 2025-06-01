package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieCountry;
import com.example.movie_streaming.movieService.model.entity.MovieCountryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieCountryRepository extends JpaRepository<MovieCountry, MovieCountryId> {
    List<MovieCountry> findByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM MovieCountry mc WHERE mc.movie.id = :movieId")
    void deleteByMovieId(Long movieId);
}