package com.example.movie_streaming.common.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Bean
    public RestTemplateBuilder restTemplate() {
        return new RestTemplateBuilder();
    }
}