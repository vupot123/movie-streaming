package com.example.movie_streaming.userService.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FavoriteId.class)
public class Favorite {
    @Id
    @Column(name = "user_id")
    private Long userId; // Thêm userId để khớp với FavoriteId

    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false) // Liên kết với user_id
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}