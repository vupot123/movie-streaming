package com.example.movie_streaming.streamService;

import com.example.movie_streaming.common.configuration.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.example.movie_streaming")
@EnableConfigurationProperties(JwtProperties.class)

public class StreamServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StreamServiceApplication.class, args);
    }
}