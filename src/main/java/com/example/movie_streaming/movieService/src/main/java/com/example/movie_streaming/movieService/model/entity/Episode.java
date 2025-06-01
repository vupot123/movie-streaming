package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "episodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "season_id")
    @JsonBackReference
    private Season season;

    @Column(name = "episode_number")
    private Integer episodeNumber;

    @Column(name = "dubbed_url")
    private String dubbedUrl;

    @Column(name = "subtitle_url")
    private String subtitleUrl;
}