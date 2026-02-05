package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.CinemaDTO;
import com.cinebook.backend.services.interfaces.ICinemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cinemas", description = "Endpoints para gestionar cines")
@CrossOrigin(origins = "*")
public class CinemaController {

    private final ICinemaService cinemaService;

    @GetMapping
    @Operation(summary = "Obtener todos los cines activos")
    public ResponseEntity<List<CinemaDTO>> getAllActiveCinemas() {
        log.info("GET /api/cinemas - Obteniendo todos los cines activos");
        List<CinemaDTO> cinemas = cinemaService.getAllActiveCinemas();
        return ResponseEntity.ok(cinemas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un cine por ID")
    public ResponseEntity<CinemaDTO> getCinemaById(@PathVariable Long id) {
        log.info("GET /api/cinemas/{} - Obteniendo cine", id);
        CinemaDTO cinema = cinemaService.getCinemaById(id);
        return ResponseEntity.ok(cinema);
    }
}