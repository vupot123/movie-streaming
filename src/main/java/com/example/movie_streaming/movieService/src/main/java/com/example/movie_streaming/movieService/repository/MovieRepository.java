package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    @Query("""
                SELECT DISTINCT m FROM Movie m
                LEFT JOIN m.movieActors ma
                LEFT JOIN ma.actor a
                WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Movie> searchByTitleOrActorName(@Param("keyword") String keyword);

}