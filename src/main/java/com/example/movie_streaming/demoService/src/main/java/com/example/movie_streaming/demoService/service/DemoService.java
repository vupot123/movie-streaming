package com.example.movie_streaming.demoService.service;

import com.example.movie_streaming.common.exceptions.EXDemo;
import com.example.movie_streaming.demoService.mapper.DemoMapper;
import com.example.movie_streaming.demoService.model.dto.DemoDTO;
import com.example.movie_streaming.demoService.model.entity.Demo;
import com.example.movie_streaming.demoService.repository.DemoRepository;
import com.example.movie_streaming.demoService.kafka.KafkaProducerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemoService {
    private final DemoRepository demoRepository;
    private final KafkaProducerService kafkaProducerService;

    public DemoService(DemoRepository demoRepository, KafkaProducerService kafkaProducerService) {
        this.demoRepository = demoRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public List<DemoDTO> findAll() {
        List<DemoDTO> demos = demoRepository.findAll()
                .stream()
                .map(DemoMapper::toDTO)
                .collect(Collectors.toList());
        sendKafkaMessage("Fetched all demos");
        return demos;
    }

    public DemoDTO findById(Long id) {
        Demo demo = demoRepository.findById(id)
                .orElseThrow(() -> new EXDemo.DemoException("Demo not found with id: " + id));
        sendKafkaMessage("Fetched demo with ID: " + id);
        return DemoMapper.toDTO(demo);
    }

    public DemoDTO create(Demo demo) {
        // Save the demo to the database
        Demo savedDemo = demoRepository.save(demo);

        // Send a Kafka message about the new demo creation
        sendKafkaMessage("Created demo with ID: " + savedDemo.getId());

        // Return the created demo DTO
        return DemoMapper.toDTO(savedDemo);
    }

    public void delete(Long id) {
        // Delete the demo from the database
        demoRepository.deleteById(id);

        // Send a Kafka message about the demo deletion
        sendKafkaMessage("Deleted demo with ID: " + id);
    }

    private void sendKafkaMessage(String message) {
        kafkaProducerService.sendMessage("your-topic", message);
    }
}