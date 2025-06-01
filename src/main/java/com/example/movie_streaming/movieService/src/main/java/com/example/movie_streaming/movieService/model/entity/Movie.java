package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private MovieType type;

    private Integer year;
    private Integer duration;
    private String intro;

    @Column(name = "age_rating")
    private String ageRating;

    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long views;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<MovieActor> movieActors;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<MovieGenre> movieGenres;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<MovieCountry> movieCountries;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<MovieTrailer> trailers;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<MovieBanner> banners;

    @Transient
    private String streamUrl;

    @OneToMany(mappedBy = "movie")
    @JsonManagedReference
    @JsonIgnore
    private Set<Season> seasons;

    @OneToMany(mappedBy = "movie", fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private Set<CollectionMovie> collectionMovies;
}