package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "movie_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {
    @EmbeddedId
    private MovieGenreId id = new MovieGenreId();

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    @JsonBackReference
    private Movie movie;

    @ManyToOne
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    @JsonManagedReference
    private Genre genre;
}