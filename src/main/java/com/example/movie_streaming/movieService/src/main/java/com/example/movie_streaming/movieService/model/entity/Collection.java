package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean featured;

    @OneToMany(mappedBy = "collection")
    @JsonManagedReference
    private List<CollectionMovie> collectionMovies;
}