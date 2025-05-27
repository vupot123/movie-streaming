package com.example.movie_streaming.streamService.repository;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SingleMovieStreamRepository extends JpaRepository<SingleMovieStream, Long> {
    // Kiểm tra xem fileUrl đã tồn tại chưa
    boolean existsByFileUrl(String fileUrl);

    // Tìm bản ghi theo fileName
    Optional<SingleMovieStream> findByFileName(String fileName);

    // Tìm tất cả bản ghi theo movieId
    List<SingleMovieStream> findAllByMovieId(Long movieId);

    // Tìm bản ghi theo movieId và fileName
    Optional<SingleMovieStream> findByMovieIdAndFileName(Long movieId, String fileName);

    // Xóa bản ghi theo movieId
    void deleteByMovieId(Long movieId);

    // Xóa bản ghi theo movieId và fileName
    void deleteByMovieIdAndFileName(Long movieId, String fileName);
}