package com.example.movie_streaming.demoService.service;

import com.example.movie_streaming.demoService.kafka.KafkaProducerService;
import com.example.movie_streaming.demoService.mapper.DemoMapper;
import com.example.movie_streaming.demoService.model.dto.DemoDTO;
import com.example.movie_streaming.demoService.model.entity.Demo;
import com.example.movie_streaming.demoService.repository.DemoRepository;
import com.example.movie_streaming.shared.kafka.KafkaMessage;
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
        sendKafkaMessage("READ_ALL", null, null);
        return demos;
    }

    public DemoDTO findById(Long id) {
        Demo demo = demoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demo not found with id: " + id));
        sendKafkaMessage("READ_ONE", id, null);
        return DemoMapper.toDTO(demo);
    }

    public DemoDTO create(Demo demo) {
        Demo savedDemo = demoRepository.save(demo);
        sendKafkaMessage("CREATE", savedDemo.getId(), Map.of("name", savedDemo.getName()));
        return DemoMapper.toDTO(savedDemo);
    }

    public void delete(Long id) {
        demoRepository.deleteById(id);
        sendKafkaMessage("DELETE", id, null);
    }

    private void sendKafkaMessage(String action, Long entityId, Map<String, Object> payload) {
        KafkaMessage kafkaMessage = new KafkaMessage("demo", action, entityId, payload);
        String messageJson = convertToJson(kafkaMessage);
        kafkaProducerService.sendMessage("demo-topic", messageJson);
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
