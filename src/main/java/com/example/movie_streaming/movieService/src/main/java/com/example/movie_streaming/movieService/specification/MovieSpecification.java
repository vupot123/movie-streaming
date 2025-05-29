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

        // Filter theo tiêu đề (keyword)
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.getKeyword().toLowerCase() + "%"));
        }

        // Filter theo type (enum: "le" hoặc "bo")
        if (filter.getType() != null && !filter.getType().isBlank()) {
            try {
                MovieType type = MovieType.fromString(filter.getType());
                predicates.add(cb.equal(root.get("type"), type));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Movie type không hợp lệ: " + filter.getType());
            }
        }

        // Filter theo năm phát hành
        if (filter.getYears() != null && !filter.getYears().isEmpty()) {
            predicates.add(root.get("year").in(filter.getYears()));
        }

        // Filter theo độ tuổi (rating)
        if (filter.getRating() != null && !filter.getRating().isBlank()) {
            predicates.add(cb.equal(root.get("ageRating"), filter.getRating()));
        }

        // Filter theo thể loại (genres)
        if (filter.getGenres() != null && !filter.getGenres().isEmpty()) {
            Join<Movie, MovieGenre> mgJoin = root.join("movieGenres", JoinType.LEFT);
            Join<MovieGenre, Genre> genreJoin = mgJoin.join("genre");
            predicates.add(genreJoin.get("name").in(filter.getGenres()));
        }

        // Filter theo quốc gia (countries)
        if (filter.getCountries() != null && !filter.getCountries().isEmpty()) {
            Join<Movie, MovieCountry> mcJoin = root.join("movieCountries", JoinType.LEFT);
            Join<MovieCountry, Country> countryJoin = mcJoin.join("country");
            predicates.add(countryJoin.get("name").in(filter.getCountries()));
        }


        // Filter theo phiên bản (versions: phụ đề, lồng tiếng...)
        // Giả sử versions được lưu trong bảng episodes (cần join qua seasons và episodes)
        if (filter.getVersions() != null && !filter.getVersions().isEmpty()) {
            Join<Movie, Season> seasonJoin = root.join("seasons", JoinType.LEFT);
            Join<Season, Episode> episodeJoin = seasonJoin.join("episodes", JoinType.LEFT);
            Predicate dubbedPredicate = cb.and(
                    episodeJoin.get("dubbedUrl").isNotNull(),
                    cb.isTrue(cb.literal(filter.getVersions().contains("dubbed")))
            );
            Predicate subtitlePredicate = cb.and(
                    episodeJoin.get("subtitleUrl").isNotNull(),
                    cb.isTrue(cb.literal(filter.getVersions().contains("subtitled")))
            );
            predicates.add(cb.or(dubbedPredicate, subtitlePredicate));
        }

        // Sắp xếp
        if (query.getResultType() != Long.class && filter.getSort() != null && !filter.getSort().isBlank()) {
            switch (filter.getSort().toLowerCase()) {
                case "views":
                    query.orderBy(cb.desc(root.get("views")));
                    break;
                case "release_date":
                case "year":
                    query.orderBy(cb.desc(root.get("year")));
                    break;
                case "title":
                    query.orderBy(cb.asc(root.get("title")));
                    break;
                default:
                    query.orderBy(cb.desc(root.get("id"))); // Mặc định sắp xếp theo ID
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}