package com.example.movie_streaming.demoService.controller;
import com.example.movie_streaming.demoService.model.dto.DemoDTO;
import com.example.movie_streaming.demoService.model.entity.Demo;
import com.example.movie_streaming.demoService.service.DemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demos")
public class DemoController {
    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping
    public List<DemoDTO> getAllDemos() {
        return demoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemoDTO> getDemoById(@PathVariable Long id) {
        return ResponseEntity.ok(demoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DemoDTO> createDemo(@RequestBody Demo demo) {
        return ResponseEntity.ok(demoService.create(demo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDemo(@PathVariable Long id) {
        demoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}