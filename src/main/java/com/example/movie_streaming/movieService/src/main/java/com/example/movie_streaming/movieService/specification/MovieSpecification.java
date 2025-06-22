package com.example.movie_streaming.movieService.specification;

import com.example.movie_streaming.movieService.model.dto.request.MovieFilterRequest;
import com.example.movie_streaming.movieService.model.entity.*;
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

        // Đảm bảo không trùng dòng khi join nhiều bảng
        query.distinct(true);

        // Filter theo từ khóa (title hoặc actor.name)
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), keyword);

            Join<Movie, MovieActor> movieActorJoin = root.join("movieActors", JoinType.LEFT);
            Join<MovieActor, Actor> actorJoin = movieActorJoin.join("actor", JoinType.LEFT);
            Predicate actorNamePredicate = cb.like(cb.lower(actorJoin.get("name")), keyword);

            predicates.add(cb.or(titlePredicate, actorNamePredicate));
        }

        // Filter theo type
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
        if (filter.getRating() != null && !filter.getRating().isBlank()) {
            predicates.add(cb.equal(root.get("ageRating"), filter.getRating()));
        }

        // Filter theo thể loại
        if (filter.getGenres() != null && !filter.getGenres().isEmpty()) {
            Join<Movie, MovieGenre> mgJoin = root.join("movieGenres", JoinType.LEFT);
            Join<MovieGenre, Genre> genreJoin = mgJoin.join("genre", JoinType.LEFT);
            predicates.add(genreJoin.get("name").in(filter.getGenres()));
        }

        // Filter theo quốc gia
        if (filter.getCountries() != null && !filter.getCountries().isEmpty()) {
            Join<Movie, Country> countryJoin = root.join("country", JoinType.LEFT);
            predicates.add(countryJoin.get("name").in(filter.getCountries()));
        }

        // Filter theo phiên bản (sub/dub)
        if (filter.getVersions() != null && !filter.getVersions().isEmpty()) {
            Join<Movie, Season> seasonJoin = root.join("seasons", JoinType.LEFT);
            Join<Season, Episode> episodeJoin = seasonJoin.join("episodes", JoinType.LEFT);

            List<Predicate> versionPredicates = new ArrayList<>();

            if (filter.getVersions().contains("dubbed")) {
                versionPredicates.add(cb.isNotNull(episodeJoin.get("dubbedUrl")));
            }
            if (filter.getVersions().contains("subtitled")) {
                versionPredicates.add(cb.isNotNull(episodeJoin.get("subtitleUrl")));
            }

            if (!versionPredicates.isEmpty()) {
                predicates.add(cb.or(versionPredicates.toArray(new Predicate[0])));
            }
        }

        // Sắp xếp
        if (query.getResultType() != Long.class && filter.getSort() != null && !filter.getSort().isBlank()) {
            switch (filter.getSort().toLowerCase()) {
                case "views" -> query.orderBy(cb.desc(root.get("views")));
                case "release_date", "year" -> query.orderBy(cb.desc(root.get("year")));
                case "title" -> query.orderBy(cb.asc(root.get("title")));
                default -> query.orderBy(cb.desc(root.get("id")));
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
