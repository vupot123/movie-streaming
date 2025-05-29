package com.example.movie_streaming.userService.repository;

import com.example.movie_streaming.userService.model.entity.MovieView;
import com.example.movie_streaming.userService.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieViewRepository extends JpaRepository<MovieView, Long> {
    List<MovieView> findByUser(User user);
}
