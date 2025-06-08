package com.example.movie_streaming.streamService.repository;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SingleMovieStreamRepository extends JpaRepository<SingleMovieStream, Long> {
    boolean existsByFileUrl(String fileUrl);

    List<SingleMovieStream> findAllByOrderByIdAsc();

    boolean existsByFileName(String fileName);

    Optional<SingleMovieStream> findByFileName(String fileName);

    void deleteByFileName(String fileName);
}