package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.MovieDTO;
import com.cinebook.backend.entities.Movie;
import com.cinebook.backend.repositories.MovieRepository;
import com.cinebook.backend.services.interfaces.IMovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements IMovieService {

    private final MovieRepository movieRepository;

    @Override
    public List<MovieDTO> getAllActiveMovies() {
        log.info("Obteniendo todas las películas activas");
        return movieRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MovieDTO getMovieById(Long id) {
        log.info("Obteniendo película con ID: {}", id);
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));
        return convertToDTO(movie);
    }

    @Override
    public List<MovieDTO> getAllMovies() {
        log.info("Obteniendo todas las películas");
        return movieRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setGenre(movie.getGenre());
        dto.setRating(movie.getRating());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setReleaseDate(movie.getReleaseDate());
        return dto;
    }
}