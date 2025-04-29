package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.response.MovieResponse;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        System.out.println("Movie: " + movie); // 👈 debug

        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle() != null ? movie.getTitle() : "Unknown")
                .type(movie.getType() != null ? movie.getType().name() : "le")
                .year(movie.getYear() != null ? movie.getYear() : 2000)
                .duration(movie.getDuration() != null ? movie.getDuration() : 0)
                .intro(movie.getIntro() != null ? movie.getIntro() : "")
                .ageRating(movie.getAgeRating() != null ? movie.getAgeRating() : "N/A")
                .views(movie.getViews() != null ? movie.getViews() : 0L)
                .build();
    }

}
