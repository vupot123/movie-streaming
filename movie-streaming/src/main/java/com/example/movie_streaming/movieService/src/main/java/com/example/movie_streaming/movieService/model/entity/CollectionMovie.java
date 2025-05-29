package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "collection_movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionMovie {
    @EmbeddedId
    private CollectionMovieId id = new CollectionMovieId();

    @ManyToOne
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id")
    @JsonManagedReference
    private Collection collection;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    @JsonBackReference
    private Movie movie;
}