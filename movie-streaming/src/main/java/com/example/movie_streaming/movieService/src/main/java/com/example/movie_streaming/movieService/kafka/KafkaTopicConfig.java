package com.example.movie_streaming.movieService.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic movieTopic() {
        return TopicBuilder.name("movie-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic movieTopicDlq() {
        return TopicBuilder.name("movie-topic-dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }
}