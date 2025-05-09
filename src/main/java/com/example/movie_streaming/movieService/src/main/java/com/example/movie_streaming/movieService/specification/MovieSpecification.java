package com.example.movie_streaming.movieService.specification;

import com.example.movie_streaming.movieService.model.dto.request.MovieFilterRequest;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.model.entity.MovieType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MovieSpecification implements Specification<Movie> {

    private final MovieFilterRequest filter;

    public MovieSpecification(MovieFilterRequest filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Filter theo tiêu đề (keyword)
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getKeyword().toLowerCase() + "%"));
        }

        // Filter theo type (enum)
        if (filter.getType() != null && !filter.getType().isBlank()) {
            try {
                MovieType type = MovieType.fromString(filter.getType());
                predicates.add(cb.equal(root.get("type"), type));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Movie type không hợp lệ: " + filter.getType());
            }
        }

        // Filter theo năm
        if (filter.getYears() != null && !filter.getYears().isEmpty()) {
            predicates.add(root.get("year").in(filter.getYears()));
        }

        // Filter theo độ tuổi
        if (filter.getRating() != null) {
            predicates.add(cb.equal(root.get("ageRating"), filter.getRating()));
        }

        // Filter theo status
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        // Filter loại trừ status
        if (filter.getExcludeStatus() != null) {
            predicates.add(cb.notEqual(root.get("status"), filter.getExcludeStatus()));
        }

        // Filter theo genre
        if (filter.getGenres() != null && !filter.getGenres().isEmpty()) {
            Join<Movie, ?> mgJoin = root.join("movieGenres", JoinType.LEFT);
            Join<?, ?> genreJoin = mgJoin.join("genre");
            predicates.add(genreJoin.get("name").in(filter.getGenres()));
        }

        // Filter theo country
        if (filter.getCountries() != null && !filter.getCountries().isEmpty()) {
            Join<Movie, ?> mcJoin = root.join("movieCountries", JoinType.LEFT);
            Join<?, ?> countryJoin = mcJoin.join("country");
            predicates.add(countryJoin.get("name").in(filter.getCountries()));
        }

        // Sắp xếp
        if (query.getResultType() != Long.class && filter.getSort() != null) {
            switch (filter.getSort()) {
                case "views" -> query.orderBy(cb.desc(root.get("views")));
                case "release_date", "year" -> query.orderBy(cb.desc(root.get("year")));
                case "title" -> query.orderBy(cb.asc(root.get("title")));
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
