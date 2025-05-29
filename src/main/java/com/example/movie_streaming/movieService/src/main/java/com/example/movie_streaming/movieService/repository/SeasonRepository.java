package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    List<Season> findByMovieId(Long movieId);

    @Modifying
    @Query("DELETE FROM Season s WHERE s.movie.id = :movieId")
    void deleteByMovieId(Long movieId);
}