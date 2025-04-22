package com.example.movie_streaming.demoService.service;

import com.example.movie_streaming.demoService.kafka.KafkaMessage;
import com.example.movie_streaming.demoService.mapper.DemoMapper;
import com.example.movie_streaming.demoService.model.dto.DemoDTO;
import com.example.movie_streaming.demoService.model.entity.Demo;
import com.example.movie_streaming.demoService.repository.DemoRepository;
import com.example.movie_streaming.demoService.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
        sendKafkaMessage("Fetched all demos", null, null);
        return demos;
    }

    public DemoDTO findById(Long id) {
        Demo demo = demoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demo not found with id: " + id));
        sendKafkaMessage("Fetched demo with ID: " + id, null, null);
        return DemoMapper.toDTO(demo);
    }

    public DemoDTO create(Demo demo) {

        Demo savedDemo = demoRepository.save(demo);


        sendKafkaMessage("Created demo with ID: " + savedDemo.getId(), savedDemo.getId(), Map.of("name", savedDemo.getName()));

        return DemoMapper.toDTO(savedDemo);
    }

    public void delete(Long id) {

        demoRepository.deleteById(id);


        sendKafkaMessage("Deleted demo with ID: " + id, id, null);
    }

    private void sendKafkaMessage(String action, Long demoId, Map<String, Object> updatedFields) {

        KafkaMessage kafkaMessage = new KafkaMessage(demoId, action, updatedFields);


        String messageJson = convertToJson(kafkaMessage);

        // Gửi thông điệp Kafka
        kafkaProducerService.sendMessage("service1-topic", messageJson);  // service2 sẽ lắng nghe topic này
    }

    private String convertToJson(KafkaMessage message) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}
