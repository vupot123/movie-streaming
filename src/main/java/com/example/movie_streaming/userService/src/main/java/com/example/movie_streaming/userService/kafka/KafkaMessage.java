package com.example.movie_streaming.userService.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    private String username;
    private String email;
    private String action; // "REGISTER", "ADD_FAVORITE"
}
