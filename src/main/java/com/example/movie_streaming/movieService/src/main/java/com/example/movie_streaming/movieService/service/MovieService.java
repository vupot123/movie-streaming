package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.common.exceptions.ResourceNotFoundException;
import com.example.movie_streaming.movieService.kafka.KafkaMessage;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
import com.example.movie_streaming.movieService.model.dto.request.*;
import com.example.movie_streaming.movieService.model.dto.response.MovieResponse;
import com.example.movie_streaming.movieService.model.entity.*;
import com.example.movie_streaming.movieService.model.entity.Collection;
import com.example.movie_streaming.movieService.repository.*;
import com.example.movie_streaming.movieService.specification.MovieSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieTrailerRepository trailerRepository;
    private final MovieBannerRepository bannerRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MovieActorRepository movieActorRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final SeasonRepository seasonRepository;
    private final ActorRepository actorRepository;
    private final GenreRepository genreRepository;
    private final CountryRepository countryRepository;
    private final CollectionRepository collectionRepository;
    private final EpisodeRepository episodeRepository;
    private final CollectionMovieRepository collectionMovieRepository;
    private final MovieMapper movieMapper;

    @Transactional(readOnly = true)
    public Page<MovieResponse> filterMovies(MovieFilterRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() - 1 : 0;
        int size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(new MovieSpecification(request), pageable)
                .map(movieMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> getAllOrSearch(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Movie> movies = (keyword != null && !keyword.isBlank()) ?
                movieRepository.searchByTitleOrActorName(keyword.trim()) :
                movieRepository.findAll();
        int start = Math.min(page * size, movies.size());
        int end = Math.min(start + size, movies.size());
        List<MovieResponse> response = movies.subList(start, end).stream()
                .map(movieMapper::toResponse)
                .toList();
        return new PageImpl<>(response, pageable, movies.size());
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findFullById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với ID: " + id));
        return movieMapper.toResponse(movie);
    }

    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        validateCreateRequest(request);
        if (movieRepository.existsByTitleIgnoreCase(request.getTitle().trim())) {
            throw new IllegalArgumentException("Phim đã tồn tại: " + request.getTitle());
        }

        List<Long> newActorIds = Optional.ofNullable(request.getNewActors()).orElse(List.of()).stream()
                .map(movieMapper::toActorEntity)
                .map(actorRepository::save)
                .map(Actor::getId)
                .toList();

        Set<Long> actorIds = new HashSet<>(newActorIds);
        Optional.ofNullable(request.getActorIds()).ifPresent(actorIds::addAll);

        Movie movie = movieRepository.save(Movie.builder()
                .title(request.getTitle().trim())
                .subtitle(Optional.ofNullable(request.getSubtitle()).orElse("").trim())
                .type(MovieType.fromString(request.getType()))
                .year(request.getYear())
                .duration(request.getDuration())
                .intro(Optional.ofNullable(request.getIntro()).orElse("").trim())
                .ageRating(request.getAgeRating())
                .views(Optional.ofNullable(request.getViews()).orElse(0L))
                .build());

        List<Integer> genreIds = getGenreIdsFromRequest(request.getGenreNames());
        Integer countryId = getCountryIdFromRequest(request.getCountryName());

        saveMovieRelations(movie, new ArrayList<>(actorIds), genreIds, countryId,
                request.getSmallBanner(), request.getLargeBanner());
        deleteAndSaveCollections(movie, request.getCollections());
        deleteAndSaveSeasons(movie, request.getSeasons());

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("movie", "CREATE", movie.getId(),
                KafkaPayloadBuilder.buildCreatePayload(request)));
        return movieMapper.toResponse(movie);
    }

    @Transactional
    public MovieResponse updateMovie(Long id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với ID: " + id));
        validateDuplicateTitle(request.getTitle(), movie);

        Set<Long> actorIds = Optional.ofNullable(request.getNewActors()).orElse(List.of()).stream()
                .map(movieMapper::toActorEntity)
                .map(actorRepository::save)
                .map(Actor::getId)
                .collect(Collectors.toSet());
        Optional.ofNullable(request.getActorIds()).ifPresent(actorIds::addAll);

        List<Integer> genreIds = getGenreIdsFromRequest(request.getGenreNames());
        Integer countryId = getCountryIdFromRequest(request.getCountryName());

//        deleteActorsGenresAndBanner(id);
//        saveMovieRelations(movie, new ArrayList<>(actorIds), genreIds, countryId,
//                request.getSmallBanner(), request.getLargeBanner());

        mergeMovieRelations(movie, new ArrayList<>(actorIds), genreIds);

        deleteAndSaveCollections(movie, request.getCollections());
        deleteAndSaveSeasons(movie, request.getSeasons());
        updateMovieFields(movie, request);
        movieRepository.save(movie);

        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("movie", "UPDATE", id, null));
        return movieMapper.toResponse(
                movieRepository.findFullById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim sau cập nhật: " + id)));
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phim với ID: " + id);
        }
        deleteMovieRelations(id);
        movieRepository.deleteById(id);
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("movie", "DELETE", id, null));
    }

    @Transactional
    public void addView(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với ID: " + id));
        movie.setViews(Optional.ofNullable(movie.getViews()).orElse(0L) + 1);
        movieRepository.save(movie);
        kafkaProducerService.sendMessage("movie-topic", new KafkaMessage("movie", "VIEW", id,
                KafkaPayloadBuilder.buildViewPayload(id, movie.getViews())));
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm không hợp lệ");
        }
        return movieRepository.searchByTitleOrActorName(keyword.trim()).stream()
                .map(movieMapper::toResponse)
                .toList();
    }

    private void deleteMovieRelations(Long movieId) {
        List<Long> seasonIds = seasonRepository.findByMovieId(movieId).stream().map(Season::getId).toList();
        episodeRepository.deleteBySeasonIds(seasonIds);
        seasonRepository.deleteByMovieId(movieId);
        movieActorRepository.deleteByMovieId(movieId);
        movieGenreRepository.deleteByMovieId(movieId);
        trailerRepository.deleteByMovieId(movieId);
        bannerRepository.deleteByMovieId(movieId);
        collectionMovieRepository.deleteByMovieId(movieId);
    }

    private void deleteActorsGenresAndBanner(Long movieId) {
        movieActorRepository.deleteByMovieId(movieId);
        movieGenreRepository.deleteByMovieId(movieId);
        bannerRepository.deleteByMovieId(movieId);
        movieActorRepository.flush();
        movieGenreRepository.flush();
        bannerRepository.flush();
    }

    private void deleteAndSaveCollections(Movie movie, List<Long> collectionIds) {
        collectionMovieRepository.deleteByMovieId(movie.getId());
        if (collectionIds != null && !collectionIds.isEmpty()) {
            List<CollectionMovie> collectionMovies = collectionIds.stream().distinct().map(cid ->
                    new CollectionMovie(new CollectionMovieId(movie.getId(), cid),
                            Collection.builder().id(cid).build(), movie)).toList();
            collectionMovieRepository.saveAll(collectionMovies);
        }
    }

    private void deleteAndSaveSeasons(Movie movie, List<CreateSeasonRequest> seasonRequests) {
        if (seasonRequests == null || seasonRequests.isEmpty()) return;
        List<Season> oldSeasons = seasonRepository.findByMovieId(movie.getId());
        List<Long> seasonIds = oldSeasons.stream().map(Season::getId).toList();
        episodeRepository.deleteBySeasonIds(seasonIds);
        seasonRepository.deleteByMovieId(movie.getId());

        List<Season> newSeasons = new ArrayList<>();
        for (CreateSeasonRequest s : seasonRequests) {
            Season season = seasonRepository.save(Season.builder()
                    .movie(movie)
                    .seasonNumber(s.getSeasonNumber())
                    .name(s.getName()).build());
            Set<Episode> episodes = Optional.ofNullable(s.getEpisodes()).orElse(List.of()).stream()
                    .map(e -> Episode.builder()
                            .season(season)
                            .episodeNumber(e.getEpisodeNumber())
                            .dubbedUrl(e.getDubbed())
                            .subtitleUrl(e.getSubbed()).build())
                    .collect(Collectors.toSet());
            episodeRepository.saveAll(episodes);
            season.setEpisodes(episodes);
            newSeasons.add(season);
        }
        movie.setSeasons(new HashSet<>(newSeasons));
    }

    private void saveMovieRelations(Movie movie, List<Long> actorIds, List<Integer> genreIds, Integer countryId, String smallBanner, String largeBanner) {
        if (!actorIds.isEmpty()) {
            List<MovieActor> movieActors = actorIds.stream().distinct().map(actorId ->
                    new MovieActor(new MovieActorId(movie.getId(), actorId), movie,
                            actorRepository.findById(actorId).orElseThrow(() -> new ResourceNotFoundException("Actor not found: " + actorId)))).toList();
            movieActorRepository.saveAll(movieActors);
            movie.setMovieActors(new HashSet<>(movieActors));
        }
        if (!genreIds.isEmpty()) {
            List<MovieGenre> movieGenres = genreIds.stream().distinct().map(genreId ->
                    new MovieGenre(new MovieGenreId(movie.getId(), genreId), movie,
                            genreRepository.findById(genreId).orElseThrow(() -> new ResourceNotFoundException("Genre not found: " + genreId)))).toList();
            movieGenreRepository.saveAll(movieGenres);
            movie.setMovieGenres(new HashSet<>(movieGenres));
        }
        if (countryId != null && countryId > 0) {
            Country country = countryRepository.findById(countryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Country not found: " + countryId));
            movie.setCountry(country);
        }
        if ((smallBanner != null && !smallBanner.isBlank()) || (largeBanner != null && !largeBanner.isBlank())) {
            MovieBanner banner = new MovieBanner(null, movie,
                    Optional.ofNullable(smallBanner).orElse(""),
                    Optional.ofNullable(largeBanner).orElse(""));
            bannerRepository.save(banner);
            movie.setBanner(banner);
        }
    }

    private List<Integer> getGenreIdsFromRequest(List<String> genreNames) {
        if (genreNames == null) return List.of();
        return genreNames.stream()
                .map(String::trim)
                .distinct()
                .map(name -> genreRepository.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new IllegalArgumentException("Thể loại không tồn tại: " + name)))
                .map(Genre::getId).toList();
    }

    private Integer getCountryIdFromRequest(String countryName) {
        return (countryName == null || countryName.isBlank()) ? null :
                countryRepository.findByNameIgnoreCase(countryName.trim())
                        .map(Country::getId)
                        .orElseThrow(() -> new IllegalArgumentException("Quốc gia không tồn tại: " + countryName));
    }

    private void validateDuplicateTitle(String newTitle, Movie movie) {
        if (newTitle != null && !newTitle.trim().equalsIgnoreCase(movie.getTitle())) {
            if (movieRepository.existsByTitleIgnoreCaseAndIdNot(newTitle.trim(), movie.getId())) {
                throw new IllegalArgumentException("Phim đã tồn tại: " + newTitle);
            }
        }
    }

    private void validateCreateRequest(CreateMovieRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Tiêu đề phim không được để trống");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new IllegalArgumentException("Loại phim không được để trống");
        }
        if (request.getYear() != null) {
            int year = request.getYear();
            int current = Calendar.getInstance().get(Calendar.YEAR);
            if (year < 1888 || year > current + 1) {
                throw new IllegalArgumentException("Năm phát hành không hợp lệ");
            }
        }
        if (request.getDuration() != null && request.getDuration() <= 0) {
            throw new IllegalArgumentException("Thời lượng phải lớn hơn 0");
        }
        if (request.getViews() != null && request.getViews() < 0) {
            throw new IllegalArgumentException("Lượt xem không được âm");
        }
    }
    private void updateMovieFields(Movie movie, UpdateMovieRequest request) {
        if (request.getTitle() != null) {
            movie.setTitle(request.getTitle().trim());
        }
        if (request.getSubtitle() != null) {
            movie.setSubtitle(request.getSubtitle().trim());
        }
        if (request.getType() != null) {
            movie.setType(MovieType.fromString(request.getType()));
        }
        if (request.getYear() != null) {
            movie.setYear(request.getYear());
        }
        if (request.getDuration() != null) {
            movie.setDuration(request.getDuration());
        }
        if (request.getIntro() != null) {
            movie.setIntro(request.getIntro().trim());
        }
        if (request.getAgeRating() != null) {
            movie.setAgeRating(request.getAgeRating());
        }
        if (request.getViews() != null) {
            movie.setViews(request.getViews());
        }
    }

    private void mergeMovieRelations(Movie movie, List<Long> actorIds, List<Integer> genreIds) {
        // ==== ACTORS ====
        Set<Long> currentActorIds = movie.getMovieActors().stream()
                .map(ma -> ma.getActor().getId()).collect(Collectors.toSet());
        Set<Long> newActorIdSet = new HashSet<>(actorIds);

        // Tìm actor cần xóa
        Set<Long> toRemoveActorIds = new HashSet<>(currentActorIds);
        toRemoveActorIds.removeAll(newActorIdSet);

        // Tìm actor cần thêm
        Set<Long> toAddActorIds = new HashSet<>(newActorIdSet);
        toAddActorIds.removeAll(currentActorIds);

        // Xóa actor không còn
        if (!toRemoveActorIds.isEmpty()) {
            movieActorRepository.deleteByMovieIdAndActorIds(movie.getId(), toRemoveActorIds);
            movie.getMovieActors().removeIf(ma -> toRemoveActorIds.contains(ma.getActor().getId()));
        }

        // Thêm actor mới
        if (!toAddActorIds.isEmpty()) {
            List<MovieActor> actorsToAdd = toAddActorIds.stream()
                    .map(actorId -> new MovieActor(
                            new MovieActorId(movie.getId(), actorId),
                            movie,
                            actorRepository.findById(actorId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy diễn viên ID: " + actorId))
                    )).toList();
            movieActorRepository.saveAll(actorsToAdd);
            movie.getMovieActors().addAll(actorsToAdd);
        }

        // ==== GENRES ====
        Set<Integer> currentGenreIds = movie.getMovieGenres().stream()
                .map(mg -> mg.getGenre().getId()).collect(Collectors.toSet());
        Set<Integer> newGenreIdSet = new HashSet<>(genreIds);

        // Tìm genre cần xóa
        Set<Integer> toRemoveGenreIds = new HashSet<>(currentGenreIds);
        toRemoveGenreIds.removeAll(newGenreIdSet);

        // Tìm genre cần thêm
        Set<Integer> toAddGenreIds = new HashSet<>(newGenreIdSet);
        toAddGenreIds.removeAll(currentGenreIds);

        // Xóa genre không còn
        if (!toRemoveGenreIds.isEmpty()) {
            movieGenreRepository.deleteByMovieIdAndGenreIds(movie.getId(), toRemoveGenreIds);
            movie.getMovieGenres().removeIf(mg -> toRemoveGenreIds.contains(mg.getGenre().getId()));
        }

        // Thêm genre mới
        if (!toAddGenreIds.isEmpty()) {
            List<MovieGenre> genresToAdd = toAddGenreIds.stream()
                    .map(genreId -> new MovieGenre(
                            new MovieGenreId(movie.getId(), genreId),
                            movie,
                            genreRepository.findById(genreId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại ID: " + genreId))
                    )).toList();
            movieGenreRepository.saveAll(genresToAdd);
            movie.getMovieGenres().addAll(genresToAdd);
        }
    }


}
