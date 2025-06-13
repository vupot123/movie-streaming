package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "movie_countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieCountry {
    @EmbeddedId
    private MovieCountryId id = new MovieCountryId();

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    @JsonBackReference
    private Movie movie;

    @ManyToOne
    @MapsId("countryId")
    @JoinColumn(name = "country_id")
    @JsonManagedReference
    private Country country;
}