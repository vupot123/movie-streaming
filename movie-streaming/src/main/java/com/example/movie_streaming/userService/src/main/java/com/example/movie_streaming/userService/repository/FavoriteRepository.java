package com.example.movie_streaming.userService.repository;

import com.example.movie_streaming.userService.model.entity.Favorite;
import com.example.movie_streaming.userService.model.entity.FavoriteId;
import com.example.movie_streaming.userService.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndMovieId(User user, Long movieId);
}
