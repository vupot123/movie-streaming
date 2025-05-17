package com.example.movie_streaming.movieService.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "single_movie_streams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleMovieStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "movie_id", referencedColumnName = "id", unique = true)
    private Movie movie;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;
}
