package com.example.movie_streaming.movieService.service;

import com.example.movie_streaming.movieService.model.dto.MovieDto;
import com.example.movie_streaming.movieService.model.entity.Movie;
import com.example.movie_streaming.movieService.repository.MovieRepository;
import com.example.movie_streaming.movieService.mapper.MovieMapper;
import com.example.movie_streaming.movieService.kafka.KafkaProducerService;
import com.example.movie_streaming.movieService.exception.MovieNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    // Thêm phim mới vào cơ sở dữ liệu (chấp nhận MovieDto)
    public MovieDto addMovie(MovieDto movieDto) {
        // Chuyển đổi MovieDto thành Movie entity
        Movie movie = movieMapper.toEntity(movieDto);

        // Lưu vào cơ sở dữ liệu
        Movie savedMovie = movieRepository.save(movie);

        // Phát sự kiện Kafka thông báo thêm phim mới
        String message = "New movie added: " + savedMovie.getTitle();
        kafkaProducerService.sendMessage("movie-topic", message);

        return movieMapper.toDto(savedMovie);  // Chuyển lại thành DTO và trả về
    }

    // Cập nhật thông tin một bộ phim (chấp nhận MovieDto)
    public MovieDto updateMovie(Long movieId, MovieDto movieDto) {
        // Lấy thông tin bộ phim cũ
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));

        // Cập nhật các trường trong Movie entity từ MovieDto
        existingMovie.setTitle(movieDto.getTitle());
        existingMovie.setYear(movieDto.getYear());
        existingMovie.setDuration(movieDto.getDuration());
        existingMovie.setIntro(movieDto.getIntro());
        existingMovie.setAgeRating(movieDto.getAgeRating());
        existingMovie.setViews(movieDto.getViews());

        // Lưu lại vào cơ sở dữ liệu
        movieRepository.save(existingMovie);

        // Phát sự kiện Kafka thông báo cập nhật phim
        String message = "Movie with ID " + movieId + " has been updated";
        kafkaProducerService.sendMessage("movie-topic", message);

        return movieMapper.toDto(existingMovie);  // Chuyển lại thành DTO và trả về
    }

    // Lấy thông tin phim theo ID (trả về MovieDto)
    public MovieDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + id));
        return movieMapper.toDto(movie);
    }

    // Lấy tất cả các phim và trả về danh sách MovieDto
    public List<MovieDto> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .map(movieMapper::toDto)
                .collect(Collectors.toList());
    }

    public void addView(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));
        movie.setViews(movie.getViews() + 1);
        movieRepository.save(movie);

        String message = "Movie with ID " + movieId + " has been viewed";
        kafkaProducerService.sendMessage("movie-topic", message);
    }
}
