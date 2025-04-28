package com.example.userService.model.dto.request;

import lombok.Data;

@Data
public class FavoriteRequest {
    private Long movieId;
    private String movieTitle;
}
