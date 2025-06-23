package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.movieService.kafka.KafkaMessage;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
import com.example.movie_streaming.movieService.model.dto.request.ActorRequest;
import com.example.movie_streaming.movieService.model.dto.response.ActorResponse;
import com.example.movie_streaming.movieService.model.entity.Actor;
import com.example.movie_streaming.movieService.model.entity.Gender;
import com.example.movie_streaming.movieService.model.entity.MovieActor;
import com.example.movie_streaming.movieService.repository.ActorRepository;
import com.example.movie_streaming.movieService.repository.MovieActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieMapper movieMapper;
    private final ActorMapper actorMapper;
    private final KafkaProducerService kafkaProducerService;

    @Transactional(readOnly = true)
    public Page<ActorResponse> getAllActors(Pageable pageable, String keyword) {
        Page<Actor> actors;
        if (keyword == null || keyword.trim().isEmpty()) {
            actors = actorRepository.findAll(pageable);
        } else {
            actors = actorRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        }
        return actors.map(actorMapper::toResponse);
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

        Map<String, Object> payload = new HashMap<>();
        payload.put("actorId", saved.getId());
        payload.put("name", saved.getName());
        payload.put("dob", saved.getDob());
        payload.put("gender", saved.getGender() != null ? saved.getGender().name() : null);
        payload.put("bio", saved.getBio());
        payload.put("avatarUrl", saved.getAvatarUrl());

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "CREATE", saved.getId(), payload));

        return actorMapper.toResponse(saved);
    }

    @Transactional
    public ActorResponse update(Long id, ActorRequest request) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found actor ID: " + id));

        Map<String, Object> changes = new HashMap<>();
        if (request.getName() != null) {
            actor.setName(request.getName());
            changes.put("updatedName", request.getName());
        }
        if (request.getDob() != null) {
            actor.setDob(request.getDob());
            changes.put("updatedDob", request.getDob());
        }
        if (request.getBio() != null) {
            actor.setBio(request.getBio());
            changes.put("updatedBio", request.getBio());
        }
        if (request.getGender() != null) {
            actor.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            changes.put("updatedGender", request.getGender().toUpperCase());
        }

        Actor updated = actorRepository.save(actor);

        if (!changes.isEmpty()) {
            changes.put("actorId", updated.getId());
            kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "UPDATE", updated.getId(), changes));
        }

        return actorMapper.toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        // Bước 1: Xóa các bản ghi liên quan trong movie_actor trước
        List<MovieActor> movieActors = movieActorRepository.findByActorId(id);
        if (!movieActors.isEmpty()) {
            movieActorRepository.deleteAll(movieActors);
        }

        // Bước 2: Xóa actor
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found actor ID: " + id));
        actorRepository.delete(actor);

        // Gửi message Kafka
        Map<String, Object> payload = new HashMap<>();
        payload.put("actorId", id);
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "DELETE", id, payload));
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