package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.response.MovieViewResponse;
import com.example.movie_streaming.movieService.model.entity.MovieView;
import com.example.movie_streaming.movieService.repository.MovieViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieViewService {

    private final MovieViewRepository viewRepository;

    public List<MovieViewResponse> getViewsByMovieId(Long movieId) {
        return viewRepository.findByMovieId(movieId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MovieViewResponse> getViewsByUserId(Long userId) {
        return viewRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MovieViewResponse toResponse(MovieView view) {
        return MovieViewResponse.builder()
                .id(view.getId())
                .movieId(view.getMovieId())
                .userId(view.getUserId())
                .viewedAt(view.getViewedAt())
                .build();
    }
}
