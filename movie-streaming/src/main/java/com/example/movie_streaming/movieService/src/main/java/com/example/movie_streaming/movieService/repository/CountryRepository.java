package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Integer> {
}