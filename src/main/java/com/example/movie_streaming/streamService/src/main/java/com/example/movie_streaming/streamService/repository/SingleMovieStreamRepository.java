package com.example.movie_streaming.streamService.repository;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SingleMovieStreamRepository extends JpaRepository<SingleMovieStream, Long> {
    boolean existsByFileUrl(String fileUrl);
    void deleteByFileId(Long fileId);
    List<SingleMovieStream> findAllByOrderByFileIdAsc();
}