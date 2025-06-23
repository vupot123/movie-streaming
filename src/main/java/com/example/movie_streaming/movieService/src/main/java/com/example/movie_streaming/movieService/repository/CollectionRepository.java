package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    // Phân trang cho tất cả collections
    Page<Collection> findAll(Pageable pageable);

    // Lấy collections được đánh dấu featured với phân trang
    Page<Collection> findByFeaturedTrue(Pageable pageable);

    // Lấy collections không được đánh dấu featured với phân trang
    Page<Collection> findByFeaturedFalse(Pageable pageable);

    // Tìm kiếm collections theo tên (case-insensitive) với phân trang
    Page<Collection> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}