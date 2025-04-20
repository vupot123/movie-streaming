package com.example.movie_streaming.demoService.service;

import com.example.movie_streaming.common.exceptions.EXDemo;
import com.example.movie_streaming.demoService.mapper.DemoMapper;
import com.example.movie_streaming.demoService.model.dto.DemoDTO;
import com.example.movie_streaming.demoService.model.entity.Demo;
import com.example.movie_streaming.demoService.repository.DemoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DemoService {
    private final DemoRepository demoRepository;

    public DemoService(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    public List<DemoDTO> findAll() {
        return demoRepository.findAll()
                .stream()
                .map(DemoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public DemoDTO findById(Long id) {
        Demo demo = demoRepository.findById(id)
                .orElseThrow(() -> new EXDemo.DemoException("Demo not found with id: " + id));
        return DemoMapper.toDTO(demo);
    }

    public DemoDTO create(Demo demo) {
        return DemoMapper.toDTO(demoRepository.save(demo));
    }

    public void delete(Long id) {
        demoRepository.deleteById(id);
    }
}