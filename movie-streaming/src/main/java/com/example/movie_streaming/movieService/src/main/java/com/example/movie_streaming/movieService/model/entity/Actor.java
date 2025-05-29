package com.example.movie_streaming.movieService.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "actors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dob;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String bio;

    @OneToMany(mappedBy = "actor")
    @ToString.Exclude
    @JsonIgnore
    private List<MovieActor> movieActors;

}