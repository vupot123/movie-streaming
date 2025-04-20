package com.example.movie_streaming;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MovieStreamingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieStreamingApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) {
		return args -> {
			try {
				String demoServiceUrl = "http://localhost:8081/demo";
				String response = restTemplate.getForObject(demoServiceUrl, String.class);
				System.out.println("Response from DemoService: " + response);
			} catch (Exception e) {
				System.out.println(" Không thể gọi DemoService: " + e.getMessage());
			}
		};
	}
}
