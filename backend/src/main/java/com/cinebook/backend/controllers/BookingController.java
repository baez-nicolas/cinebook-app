package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.BookingRequestDTO;
import com.cinebook.backend.dtos.BookingResponseDTO;
import com.cinebook.backend.services.interfaces.IBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bookings", description = "Endpoints para gestionar reservas")
@CrossOrigin(origins = "*")
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping
    @Operation(summary = "Crear una nueva reserva")
    public ResponseEntity<BookingResponseDTO> createBooking(
            @RequestParam(defaultValue = "user1") String userName,
            @Valid @RequestBody BookingRequestDTO request) {

        log.info("POST /api/bookings - Usuario: {} - Función: {} - Asientos: {}",
                userName, request.getShowtimeId(), request.getSeatIds());

        BookingResponseDTO response = bookingService.createBooking(userName, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Obtener todas las reservas")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        log.info("GET /api/bookings - Obteniendo todas las reservas");
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userName}")
    @Operation(summary = "Obtener todas las reservas de un usuario")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUser(@PathVariable String userName) {
        log.info("GET /api/bookings/user/{} - Obteniendo reservas", userName);
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userName);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/confirmation/{code}")
    @Operation(summary = "Obtener una reserva por código de confirmación")
    public ResponseEntity<BookingResponseDTO> getBookingByConfirmationCode(@PathVariable String code) {
        log.info("GET /api/bookings/confirmation/{} - Buscando reserva", code);
        BookingResponseDTO booking = bookingService.getBookingByConfirmationCode(code);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una reserva por ID")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        log.info("GET /api/bookings/{} - Obteniendo reserva", id);
        BookingResponseDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }
}