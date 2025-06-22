package com.example.movie_streaming.movieService.model.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorRequest {
    private String name;
    private String gender; // "male", "female", "other"
    private LocalDate dob;
    //private String avatarUrl;
    private String bio;
}
