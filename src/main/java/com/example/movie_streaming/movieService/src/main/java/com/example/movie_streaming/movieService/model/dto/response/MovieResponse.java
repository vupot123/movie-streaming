package com.example.movie_streaming.movieService.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class MovieResponse {
    private Long id;
    private String title;
    private String type;
    private Integer year;
    private Integer duration;
    private String intro;
    private String ageRating;
    private Long views;
    private List<MovieTrailerResponse> trailers;
    private Set<MovieBannerResponse> banners;
    private Set<ActorResponse> actors;
    private Set<GenreResponse> genres;
    private Set<CountryResponse> countries;
    private String posterUrl;
    private Set<SeasonResponse> seasons;
    private String streamUrl;
}