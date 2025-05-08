package com.example.movie_streaming.movieService.model.dto;

public class MovieBannerDto {

    private Long movieId;
    private String smallBannerUrl;
    private String largeBannerUrl;

    public MovieBannerDto() {
    }

    // Constructor
    public MovieBannerDto(Long movieId, String smallBannerUrl, String largeBannerUrl) {
        this.movieId = movieId;
        this.smallBannerUrl = smallBannerUrl;
        this.largeBannerUrl = largeBannerUrl;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getSmallBannerUrl() {
        return smallBannerUrl;
    }

    public void setSmallBannerUrl(String smallBannerUrl) {
        this.smallBannerUrl = smallBannerUrl;
    }

    public String getLargeBannerUrl() {
        return largeBannerUrl;
    }

    public void setLargeBannerUrl(String largeBannerUrl) {
        this.largeBannerUrl = largeBannerUrl;
    }
}
