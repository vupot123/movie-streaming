package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "movie_countries")
public class MovieCountry {

    @EmbeddedId
    private MovieCountryId id;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("countryId")
    @JoinColumn(name = "country_id")
    private Country country;
}
