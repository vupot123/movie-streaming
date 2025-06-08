package com.example.movie_streaming.errorService.repository;

import com.example.movie_streaming.errorService.model.entity.ErrorReport;
import com.example.movie_streaming.errorService.model.entity.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErrorReportRepository extends JpaRepository<ErrorReport, Long> {
    List<ErrorReport> findByStatus(ErrorStatus status);
}