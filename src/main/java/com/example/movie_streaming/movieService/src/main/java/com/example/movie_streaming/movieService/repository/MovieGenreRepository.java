package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.MovieGenre;
import com.example.movie_streaming.movieService.model.entity.MovieGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, MovieGenreId> {
    List<MovieGenre> findByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.id = :movieId")
    void deleteByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.id = :movieId AND mg.genre.id IN :genreIds")
    void deleteByMovieIdAndGenreIds(@Param("movieId") Long movieId, @Param("genreIds") Set<Integer> genreIds);
}