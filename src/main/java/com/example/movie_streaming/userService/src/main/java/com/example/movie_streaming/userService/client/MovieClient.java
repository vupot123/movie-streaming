package com.example.movie_streaming.userService.client;

import com.example.movie_streaming.userService.exception.MovieServiceUnavailableException;
import com.example.movie_streaming.userService.model.dto.response.ApiResponse;
import com.example.movie_streaming.userService.model.dto.response.MovieResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class MovieClient {

    private final RestTemplate restTemplate;

    public MovieResponse getMovieById(Long movieId) {
        String url = "http://movie-service:8082/api/movies/" + movieId;
        try {
            String rawJson = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            ApiResponse<MovieResponse> response = mapper.readValue(
                    rawJson,
                    new TypeReference<ApiResponse<MovieResponse>>() {
                    }
            );
            return response.getData(); // ✅ Lấy movie từ `data` field
        } catch (Exception ex) {
            ex.printStackTrace(); // Tạm log để debug
            throw new MovieServiceUnavailableException("Movie service is unavailable.");
        }
    }
}

