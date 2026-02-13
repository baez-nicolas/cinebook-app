package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.CreateMovieRequest;
import com.cinebook.backend.dtos.MovieDTO;
import com.cinebook.backend.dtos.ReassignShowtimesRequest;
import com.cinebook.backend.entities.Movie;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.enums.MovieRating;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.repositories.MovieRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Movies", description = "Gestión de películas (solo ADMIN)")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminMovieController {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;

    private static final int MAX_MOVIES = 12;

    @GetMapping("/count")
    @Operation(summary = "Contar películas activas")
    public ResponseEntity<Map<String, Object>> countActiveMovies() {
        long activeCount = movieRepository.countByIsActiveTrue();
        long totalCount = movieRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("activeMovies", activeCount);
        response.put("totalMovies", totalCount);
        response.put("maxMovies", MAX_MOVIES);
        response.put("canAddMore", activeCount < MAX_MOVIES);

        log.info("📊 Películas activas: {}/{}", activeCount, MAX_MOVIES);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Crear nueva película (requiere validación de límite)")
    public ResponseEntity<?> createMovie(@RequestBody CreateMovieRequest request) {
        log.info("🎬 Admin intentando crear película: {}", request.getTitle());

        long activeCount = movieRepository.countByIsActiveTrue();
        if (activeCount >= MAX_MOVIES) {
            log.warn("⚠️ Límite alcanzado: {}/{} películas activas", activeCount, MAX_MOVIES);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Límite alcanzado",
                            "message", "Ya hay " + MAX_MOVIES + " películas activas. Elimina una antes de agregar otra.",
                            "activeMovies", activeCount
                    ));
        }

        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setGenre(request.getGenre());
        movie.setRating(request.getRating() != null ? MovieRating.valueOf(request.getRating()) : null);
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setIsActive(true);

        movieRepository.save(movie);

        log.info("✅ Película creada: {} (ID: {})", movie.getTitle(), movie.getId());

        long orphanCount = countOrphanShowtimes();

        Map<String, Object> response = new HashMap<>();
        response.put("movie", convertToDTO(movie));
        response.put("orphanShowtimesAvailable", orphanCount);
        response.put("message", "Película creada exitosamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Actualizar película existente")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody CreateMovieRequest request) {
        log.info("✏️ Admin actualizando película ID: {}", id);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setGenre(request.getGenre());
        movie.setRating(request.getRating() != null ? MovieRating.valueOf(request.getRating()) : null);
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setReleaseDate(request.getReleaseDate());

        movieRepository.save(movie);

        log.info("✅ Película actualizada: {}", movie.getTitle());

        return ResponseEntity.ok(Map.of(
                "movie", convertToDTO(movie),
                "message", "Película actualizada exitosamente"
        ));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Eliminar película, funciones y reservas")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        log.info("🗑️ Admin eliminando película ID: {}", id);

        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Película no encontrada con ID: " + id));

        if (!movie.getIsActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La película ya está eliminada"));
        }

        List<Showtime> showtimes = showtimeRepository.findByMovieId(id);
        List<Long> showtimeIds = showtimes.stream()
                .map(Showtime::getId)
                .collect(Collectors.toList());

        int deletedBookings = 0;
        if (!showtimeIds.isEmpty()) {
            deletedBookings = bookingRepository.deleteByShowtimeIdIn(showtimeIds);
        }

        movie.setIsActive(false);
        movieRepository.save(movie);

        log.info("✅ Película eliminada: {} ({} funciones, {} reservas eliminadas)",
            movie.getTitle(), showtimes.size(), deletedBookings);

        return ResponseEntity.ok(Map.of(
                "message", "Película eliminada exitosamente",
                "movieTitle", movie.getTitle(),
                "orphanShowtimes", showtimes.size(),
                "deletedBookings", deletedBookings
        ));
    }

    @GetMapping("/orphan-showtimes/count")
    @Operation(summary = "Contar funciones huérfanas (de películas eliminadas)")
    public ResponseEntity<Map<String, Object>> getOrphanShowtimesCount() {
        long orphanCount = countOrphanShowtimes();

        log.info("📊 Funciones huérfanas: {}", orphanCount);

        return ResponseEntity.ok(Map.of(
                "orphanShowtimes", orphanCount,
                "available", orphanCount > 0
        ));
    }

    @PostMapping("/showtimes/reassign")
    @Transactional
    @Operation(summary = "Reasignar funciones huérfanas a una película nueva")
    public ResponseEntity<?> reassignShowtimes(@RequestBody ReassignShowtimesRequest request) {
        log.info("🔄 Reasignando funciones a película ID: {}", request.getMovieId());

        Movie newMovie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Película no encontrada"));

        if (!newMovie.getIsActive()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "La película no está activa"));
        }

        List<Showtime> orphanShowtimes = showtimeRepository.findAll()
                .stream()
                .filter(st -> st.getMovie() != null && !st.getMovie().getIsActive())
                .collect(Collectors.toList());

        if (orphanShowtimes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No hay funciones huérfanas disponibles"));
        }

        orphanShowtimes.forEach(showtime -> {
            showtime.setMovie(newMovie);
            showtime.setPrice(getPriceForType(showtime.getType()));
        });

        showtimeRepository.saveAll(orphanShowtimes);

        long remainingOrphans = countOrphanShowtimes();

        log.info("✅ {} funciones reasignadas a '{}'", orphanShowtimes.size(), newMovie.getTitle());

        return ResponseEntity.ok(Map.of(
                "message", orphanShowtimes.size() + " funciones reasignadas exitosamente",
                "movieId", newMovie.getId(),
                "movieTitle", newMovie.getTitle(),
                "reassignedCount", orphanShowtimes.size(),
                "remainingOrphanShowtimes", remainingOrphans
        ));
    }

    private long countOrphanShowtimes() {
        return showtimeRepository.findAll()
                .stream()
                .filter(st -> st.getMovie() != null && !st.getMovie().getIsActive())
                .count();
    }

    private BigDecimal getPriceForType(ShowtimeType type) {
        switch (type) {
            case SPANISH_2D:
                return new BigDecimal("5000");
            case SUBTITLED_2D:
                return new BigDecimal("4500");
            case SPANISH_3D:
                return new BigDecimal("8000");
            default:
                return new BigDecimal("5000");
        }
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDuration(movie.getDuration());
        dto.setGenre(movie.getGenre());
        dto.setRating(movie.getRating() != null ? movie.getRating().name() : null);
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setIsActive(movie.getIsActive());
        return dto;
    }
}

