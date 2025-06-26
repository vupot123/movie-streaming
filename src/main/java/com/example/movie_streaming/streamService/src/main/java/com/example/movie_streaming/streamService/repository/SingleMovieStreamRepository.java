package com.example.movie_streaming.streamService.repository;

import com.example.movie_streaming.streamService.model.entity.SingleMovieStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SingleMovieStreamRepository extends JpaRepository<SingleMovieStream, Long> {

    boolean existsByFileUrl(String fileUrl);

    // Giữ nguyên phương thức không phân trang
    List<SingleMovieStream> findAllByOrderByIdAsc();

    boolean existsByFileName(String fileName);

    Optional<SingleMovieStream> findByFileName(String fileName);

    @Modifying
    @Transactional
    @Query("DELETE FROM SingleMovieStream s WHERE s.fileName = :fileName")
    void deleteByFileName(String fileName);

    @Query("SELECT s FROM SingleMovieStream s WHERE LOWER(s.fileName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.fileUrl) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SingleMovieStream> findByFileNameContainingIgnoreCaseOrFileUrlContainingIgnoreCase(String search);

    // Thêm phương thức phân trang cho findAllByOrderByIdAsc
    Page<SingleMovieStream> findAllByOrderByIdAsc(Pageable pageable);

    // Thêm phương thức phân trang cho tìm kiếm
    @Query("SELECT s FROM SingleMovieStream s WHERE LOWER(s.fileName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.fileUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<SingleMovieStream> findByFileNameContainingIgnoreCaseOrFileUrlContainingIgnoreCase(String keyword, Pageable pageable);
}