package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.ShowtimeDTO;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Showtimes", description = "Endpoints para gestionar funciones")
@CrossOrigin(origins = "*")
public class ShowtimeController {

    private final IShowtimeService showtimeService;

    @GetMapping
    @Operation(summary = "Obtener todas las funciones de la semana actual")
    public ResponseEntity<List<ShowtimeDTO>> getCurrentWeekShowtimes() {
        log.info("GET /api/showtimes - Obteniendo funciones de la semana actual");
        List<ShowtimeDTO> showtimes = showtimeService.getCurrentWeekShowtimes();
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una función por ID")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Long id) {
        log.info("GET /api/showtimes/{} - Obteniendo función", id);
        ShowtimeDTO showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(showtime);
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Obtener funciones por película")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByMovie(@PathVariable Long movieId) {
        log.info("GET /api/showtimes/movie/{} - Obteniendo funciones de la película", movieId);
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByMovie(movieId);
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/cinema/{cinemaId}")
    @Operation(summary = "Obtener funciones por cine")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByCinema(@PathVariable Long cinemaId) {
        log.info("GET /api/showtimes/cinema/{} - Obteniendo funciones del cine", cinemaId);
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByCinema(cinemaId);
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/cinema/{cinemaId}/movie/{movieId}")
    @Operation(summary = "Obtener funciones por cine y película")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByCinemaAndMovie(
            @PathVariable Long cinemaId,
            @PathVariable Long movieId) {
        log.info("GET /api/showtimes/cinema/{}/movie/{} - Obteniendo funciones", cinemaId, movieId);
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByCinemaAndMovie(cinemaId, movieId);
        return ResponseEntity.ok(showtimes);
    }

    @GetMapping("/filter")
    @Operation(summary = "Obtener funciones por película, cine y fecha")
    public ResponseEntity<List<ShowtimeDTO>> getShowtimesByFilters(
            @RequestParam Long movieId,
            @RequestParam Long cinemaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("📊 Buscando funciones: Película {}, Cine {}, Fecha {}", movieId, cinemaId, date);

        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByFilters(movieId, cinemaId, date);

        log.info("✅ Encontradas {} funciones", showtimes.size());

        return ResponseEntity.ok(showtimes);
    }
}