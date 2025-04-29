package com.example.movie_streaming.movieService.mapper;

import com.example.movie_streaming.movieService.model.dto.MovieDto;
import com.example.movie_streaming.movieService.model.dto.MovieTrailerDto;
import com.example.movie_streaming.movieService.model.dto.MovieBannerDto;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.model.entity.MovieTrailer;
import com.example.movie_streaming.movieService.model.entity.MovieBanner;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieDto toDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getType(),
                movie.getYear(),
                movie.getDuration(),
                movie.getIntro(),
                movie.getAgeRating(),
                movie.getViews()
        );
    }

    public MovieTrailerDto toTrailerDto(MovieTrailer movieTrailer) {
        return new MovieTrailerDto(
                movieTrailer.getMovieId(),
                movieTrailer.getUrl()
        );
    }

    public MovieBannerDto toBannerDto(MovieBanner movieBanner) {
        return new MovieBannerDto(
                movieBanner.getMovieId(),
                movieBanner.getSmallBanner(),
                movieBanner.getLargeBanner()
        );
    }

    public Movie toEntity(MovieDto movieDto) {
        Movie movie = new Movie();
        movie.setTitle(movieDto.getTitle());
        movie.setType(movieDto.getType());
        movie.setYear(movieDto.getYear());
        movie.setDuration(movieDto.getDuration());
        movie.setIntro(movieDto.getIntro());
        movie.setAgeRating(movieDto.getAgeRating());
        movie.setViews(movieDto.getViews());

        return movie;
    }

    public MovieTrailer toTrailerEntity(MovieTrailerDto movieTrailerDto) {
        MovieTrailer movieTrailer = new MovieTrailer();
        movieTrailer.setMovieId(movieTrailerDto.getMovieId());
        movieTrailer.setUrl(movieTrailerDto.getTrailerUrl());

        return movieTrailer;
    }

    public MovieBanner toBannerEntity(MovieBannerDto movieBannerDto) {
        MovieBanner movieBanner = new MovieBanner();
        movieBanner.setMovieId(movieBannerDto.getMovieId());
        movieBanner.setSmallBanner(movieBannerDto.getSmallBannerUrl());
        movieBanner.setLargeBanner(movieBannerDto.getLargeBannerUrl());

        return movieBanner;
    }
}
