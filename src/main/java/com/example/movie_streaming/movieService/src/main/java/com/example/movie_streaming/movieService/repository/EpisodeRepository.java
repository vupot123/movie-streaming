package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.CollectionMovie;
import com.example.movie_streaming.movieService.model.entity.CollectionMovieId;
import com.example.movie_streaming.movieService.model.entity.Episode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Episode e WHERE e.season.id = :seasonId")
    void deleteBySeasonId(@Param("seasonId") Long seasonId);

    @Modifying
    @Query("DELETE FROM Episode e WHERE e.season.id IN :seasonIds")
    void deleteBySeasonIds(@Param("seasonIds") List<Long> seasonIds);

}
