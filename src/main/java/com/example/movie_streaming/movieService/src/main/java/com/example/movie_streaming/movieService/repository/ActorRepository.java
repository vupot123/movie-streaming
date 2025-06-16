package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Actor> findByNameContainingIgnoreCase(String keyword);
}