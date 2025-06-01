package com.example.movie_streaming.userService.model.entity;

import lombok.*;

import java.io.Serializable;
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteId implements Serializable {
    private Long userId;
    private Long movieId;
}