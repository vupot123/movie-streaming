package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByFeaturedTrue();
    List<Collection> findByFeaturedFalse();
    List<Collection> findByNameContainingIgnoreCase(String keyword);
}
