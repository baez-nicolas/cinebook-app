package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.SeatDTO;
import com.cinebook.backend.services.interfaces.ISeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seats", description = "Endpoints para gestionar asientos")
@CrossOrigin(origins = "*")
public class SeatController {

    private final ISeatService seatService;

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Obtener todos los asientos de una función")
    public ResponseEntity<List<SeatDTO>> getSeatsByShowtime(@PathVariable Long showtimeId) {
        log.info("GET /api/seats/showtime/{} - Obteniendo asientos", showtimeId);
        List<SeatDTO> seats = seatService.getSeatsByShowtime(showtimeId);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/showtime/{showtimeId}/available")
    @Operation(summary = "Obtener asientos disponibles de una función")
    public ResponseEntity<List<SeatDTO>> getAvailableSeatsByShowtime(@PathVariable Long showtimeId) {
        log.info("GET /api/seats/showtime/{}/available - Obteniendo asientos disponibles", showtimeId);
        List<SeatDTO> seats = seatService.getAvailableSeatsByShowtime(showtimeId);
        return ResponseEntity.ok(seats);
    }
}