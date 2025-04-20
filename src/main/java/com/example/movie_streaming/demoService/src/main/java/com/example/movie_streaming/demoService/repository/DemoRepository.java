package com.example.movie_streaming.demoService.repository;

import com.example.movie_streaming.demoService.model.entity.Demo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoRepository extends JpaRepository<Demo, Long> {
}
