package com.example.movie_streaming.demoService.mapper;

import com.example.movie_streaming.demoService.model.dto.DemoDTO;
import com.example.movie_streaming.demoService.model.entity.Demo;

public class DemoMapper {

    public static DemoDTO toDTO(Demo entity) {
        if (entity == null) {
            return null;
        }

        DemoDTO dto = new DemoDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        return dto;
    }

    public static Demo toEntity(DemoDTO dto) {
        if (dto == null) {
            return null;
        }

        Demo entity = new Demo();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        return entity;
    }
}
