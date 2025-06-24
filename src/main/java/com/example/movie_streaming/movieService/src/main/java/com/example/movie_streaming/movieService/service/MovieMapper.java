package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.request.ActorRequest;
import com.example.movie_streaming.movieService.model.dto.response.*;
import com.example.movie_streaming.movieService.model.entity.*;
import com.example.movie_streaming.movieService.model.entity.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieMapper {

    private final ActorMapper actorMapper;

    public MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .subtitle(movie.getSubtitle())
                .type(movie.getType() != null ? movie.getType().toString() : null)
                .year(movie.getYear())
                .duration(movie.getDuration())
                .intro(movie.getIntro())
                .ageRating(movie.getAgeRating())
                .views(movie.getViews())
                .smallBanner(Optional.ofNullable(movie.getBanner()).map(MovieBanner::getSmallBanner).orElse(null))
                .bigBanner(Optional.ofNullable(movie.getBanner()).map(MovieBanner::getLargeBanner).orElse(null))
                .actors(mapActors(movie.getMovieActors()))
                .genreNames(mapGenres(movie.getMovieGenres()))
                .countries(Optional.ofNullable(movie.getCountry()).map(Country::getName).orElse(null))
                .seasons(mapSeasons(movie.getSeasons()))
                .collections(mapCollections(movie.getCollectionMovies()))
                .build();
    }

//    private Set<CollectionResponse> mapCollections(Set<CollectionMovie> collectionMovies) {
//        if (collectionMovies == null) return Set.of();
//        return collectionMovies.stream()
//                .map(cm -> {
//                    var collection = cm.getCollection();
//                    return CollectionResponse.builder()
//                            .id(collection.getId())
//                            .name(collection.getName())
//                            .featured(collection.getFeatured())
//                            .movieIDs(null) // giữ null nếu không cần movieIDs
//                            .build();
//                })
//                .collect(Collectors.toSet());
//    }

    private Set<CollectionResponse> mapCollections(Set<CollectionMovie> collectionMovies) {
        if (collectionMovies == null) return Set.of();

        return collectionMovies.stream()
                .map(cm -> {
                    Collection collection = cm.getCollection();
                    Set<Long> movieIds = collection.getCollectionMovies() != null
                            ? collection.getCollectionMovies().stream()
                            .map(c -> c.getMovie().getId())
                            .collect(Collectors.toSet())
                            : Set.of();

                    return CollectionResponse.builder()
                            .id(collection.getId())
                            .name(collection.getName())
                            .featured(collection.getFeatured())
                            .movieIDs(new ArrayList<>(movieIds))
                            .build();
                })
                .collect(Collectors.toSet());
    }

    private Set<CollectionResponse> mapCollectionsBasic(Set<CollectionMovie> collectionMovies) {
        if (collectionMovies == null) return Set.of();

        return collectionMovies.stream()
                .map(cm -> {
                    Collection collection = cm.getCollection();
                    return CollectionResponse.builder()
                            .id(collection.getId())
                            .name(collection.getName())
                            .featured(collection.getFeatured())
                            .movieIDs(null)
                            .build();
                })
                .collect(Collectors.toSet());
    }


    private List<MovieTrailerResponse> mapTrailers(Set<MovieTrailer> trailers) {
        if (trailers == null) return List.of();
        return trailers.stream()
                .map(t -> new MovieTrailerResponse(t.getId(), t.getMovie().getId(), t.getUrl()))
                .toList();
    }

    private Set<MovieBannerResponse> mapBanners(Set<MovieBanner> banners) {
        if (banners == null) return Set.of();
        return banners.stream()
                .map(b -> new MovieBannerResponse(
                        b.getId(),
                        b.getMovie().getId(),
                        b.getSmallBanner(),
                        b.getLargeBanner()))
                .collect(Collectors.toSet());
    }

    private Set<ActorResponse> mapActors(Set<MovieActor> movieActors) {
        if (movieActors == null) return Set.of();
        return movieActors.stream()
                .map(ma -> actorMapper.toResponse(ma.getActor()))
                .collect(Collectors.toSet());
    }

    private Set<GenreResponse> mapGenres(Set<MovieGenre> movieGenres) {
        if (movieGenres == null) return Set.of();
        return movieGenres.stream()
                .map(g -> new GenreResponse(g.getGenre().getId(), g.getGenre().getName()))
                .collect(Collectors.toSet());
    }

    private Set<SeasonResponse> mapSeasons(Set<Season> seasons) {
        if (seasons == null) return Set.of();
        return seasons.stream()
                .map(s -> new SeasonResponse(
                        s.getId(),
                        s.getMovie().getId(),
                        s.getSeasonNumber(),
                        s.getName(),
                        mapEpisodes(s.getEpisodes(), s.getId())
                ))
                .sorted(Comparator.comparing(SeasonResponse::getSeasonNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new)); // Preserve order
    }

    private Set<EpisodeResponse> mapEpisodes(Set<Episode> episodes, Long seasonId) {
        if (episodes == null) return Set.of();
        return episodes.stream()
                .map(e -> new EpisodeResponse(
                        e.getId(),
                        seasonId,
                        e.getEpisodeNumber(),
                        e.getDubbedUrl(),
                        e.getSubtitleUrl()
                ))
                .sorted(Comparator.comparing(EpisodeResponse::getEpisodeNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new)); // Preserve order
    }

    public Actor toActorEntity(ActorRequest request) {
        Gender gender = null;
        if (request.getGender() != null) {
            try {
                gender = Gender.valueOf(request.getGender().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Giới tính không hợp lệ: " + request.getGender());
            }
        }

        return Actor.builder()
                .name(request.getName())
                .gender(gender)
                .dob(request.getDob())
                .avatarUrl(request.getAvatarUrl())
                .bio(request.getBio())
                .build();
    }
}
