package com.example.movie_streaming.movieService.repository;

import com.example.movie_streaming.movieService.model.entity.CollectionMovie;
import com.example.movie_streaming.movieService.model.entity.CollectionMovieId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollectionMovieRepository extends JpaRepository<CollectionMovie, CollectionMovieId> {
    void deleteByCollectionId(Long collectionId);
    // Lấy tất cả CollectionMovie theo collectionId
    @Query("SELECT cm FROM CollectionMovie cm WHERE cm.collection.id = :collectionId")
    List<CollectionMovie> findAllByCollectionId(Long collectionId);
}
