package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Collection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByFeaturedTrue();
    List<Collection> findByFeaturedFalse();
    List<Collection> findByNameContainingIgnoreCase(String keyword);

}
