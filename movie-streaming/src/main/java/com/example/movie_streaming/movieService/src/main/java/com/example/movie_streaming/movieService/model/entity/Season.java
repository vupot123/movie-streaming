package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonBackReference
    private Movie movie;

    @Column(name = "season_number")
    private Integer seasonNumber;

    private String name;

    @OneToMany(mappedBy = "season")
    @ToString.Exclude
    @JsonIgnore
    private Set<Episode> episodes;
}