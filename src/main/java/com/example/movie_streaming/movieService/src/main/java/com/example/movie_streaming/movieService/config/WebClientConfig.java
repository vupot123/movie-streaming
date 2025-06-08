package com.example.movie_streaming.movieService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient streamWebClient() {
        return WebClient.builder()
                .baseUrl("https://movie-streaming-stream-service-319946458144.asia-southeast1.run.app")
                .build();
    }
}

