package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.movieService.kafka.KafkaMessage;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
import com.example.movie_streaming.movieService.model.dto.request.ActorRequest;
import com.example.movie_streaming.movieService.model.dto.response.ActorResponse;
import com.example.movie_streaming.movieService.model.entity.Actor;
import com.example.movie_streaming.movieService.model.entity.Gender;
import com.example.movie_streaming.movieService.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final MovieMapper movieMapper;
    private final ActorMapper actorMapper;
    private final KafkaProducerService kafkaProducerService;

    @Transactional(readOnly = true)
    public List<ActorResponse> getAllActors() {
        return actorRepository.findAll().stream()
                .map(actorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ActorResponse getById(Long id) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found actor ID: " + id));
        return actorMapper.toResponse(actor);
    }

    @Transactional
    public ActorResponse create(ActorRequest request) {
        Actor actor = movieMapper.toActorEntity(request);
        Actor saved = actorRepository.save(actor);

        Map<String, Object> payload = Map.of(
                "actorId", saved.getId(),
                "name", saved.getName()
        );
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "CREATE", saved.getId(), payload));

        return toResponse(saved);
    }

    @Transactional
    public ActorResponse update(Long id, ActorRequest request) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found actor ID: " + id));

        if (request.getName() != null) actor.setName(request.getName());
        if (request.getDob() != null) actor.setDob(request.getDob());
        if (request.getAvatarUrl() != null) actor.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) actor.setBio(request.getBio());
        if (request.getGender() != null) {
            actor.setGender(Gender.valueOf(request.getGender().toUpperCase()));
        }

        Actor updated = actorRepository.save(actor);

        Map<String, Object> payload = Map.of(
                "actorId", updated.getId(),
                "updatedName", updated.getName()
        );
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "UPDATE", updated.getId(), payload));

        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!actorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Not found actor ID: " + id);
        }
        actorRepository.deleteById(id);

        Map<String, Object> payload = Map.of("actorId", id);
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "DELETE", id, payload));
    }

    public List<ActorResponse> search(String keyword) {
        return actorRepository.findByNameContainingIgnoreCase(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ActorResponse toResponse(Actor actor) {
        List<Long> movieIds = actor.getMovieActors() != null
                ? actor.getMovieActors().stream()
                .map(ma -> ma.getMovie().getId())
                .distinct()
                .toList()
                : List.of();

        return new ActorResponse(
                actor.getId(),
                actor.getName(),
                actor.getGender() != null ? actor.getGender().name() : null,
                actor.getDob(),
                actor.getAvatarUrl(),
                actor.getBio(),
                movieIds
        );
    }
}
