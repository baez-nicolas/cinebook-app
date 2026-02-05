package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.MovieDTO;
import com.cinebook.backend.services.interfaces.IMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Movies", description = "Endpoints para gestionar películas")
@CrossOrigin(origins = "*")
public class MovieController {

    private final IMovieService movieService;

    @GetMapping
    @Operation(summary = "Obtener todas las películas en cartelera")
    public ResponseEntity<List<MovieDTO>> getAllActiveMovies() {
        log.info("GET /api/movies - Obteniendo todas las películas activas");
        List<MovieDTO> movies = movieService.getAllActiveMovies();
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una película por ID")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        log.info("GET /api/movies/{} - Obteniendo película", id);
        MovieDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todas las películas (incluso inactivas)")
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        log.info("GET /api/movies/all - Obteniendo todas las películas");
        List<MovieDTO> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
    }
}