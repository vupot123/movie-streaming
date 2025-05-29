package com.example.errorService.repository;

import com.example.errorService.model.entity.ErrorReport;
import com.example.errorService.model.entity.ErrorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ErrorReportRepository extends JpaRepository<ErrorReport, Long> {
    List<ErrorReport> findByStatus(ErrorStatus status);
}

