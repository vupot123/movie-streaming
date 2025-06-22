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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ActorResponse> getAllActors(Pageable pageable) {
        return actorRepository.findAll(pageable)
                .map(actorMapper::toResponse);
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
        //if (request.getAvatarUrl() != null) actor.setAvatarUrl(request.getAvatarUrl());
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
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found actor ID: " + id));

        // Clear movieActors để kích hoạt orphanRemoval = true
        if (actor.getMovieActors() != null) {
            actor.getMovieActors().clear();
        }

        actorRepository.delete(actor);

        Map<String, Object> payload = Map.of("actorId", id);
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("actor", "DELETE", id, payload));
    }


//    public List<ActorResponse> search(String keyword) {
//        return actorRepository.findByNameContainingIgnoreCase(keyword)
//                .stream().map(this::toResponse).collect(Collectors.toList());
//    }

    public Page<ActorResponse> search(String keyword, Pageable pageable) {
        return actorRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(actorMapper::toResponse);
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
