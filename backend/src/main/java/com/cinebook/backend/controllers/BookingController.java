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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @Operation(summary = "Crear una nueva reserva (requiere autenticación)")
    public ResponseEntity<BookingResponseDTO> createBooking(
            Authentication authentication,
            @Valid @RequestBody BookingRequestDTO request) {

        String userEmail = authentication.getName();
        log.info("POST /api/bookings - Usuario: {} - Función: {} - Asientos: {}",
                userEmail, request.getShowtimeId(), request.getSeatIds());

        BookingResponseDTO response = bookingService.createBooking(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todas las reservas (solo ADMIN)")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        log.info("GET /api/bookings - Obteniendo todas las reservas (ADMIN)");
        List<BookingResponseDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Obtener mis reservas (requiere autenticación)")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("GET /api/bookings/my-bookings - Usuario: {}", userEmail);
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userEmail);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userEmail}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener reservas de un usuario específico (solo ADMIN)")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByUser(@PathVariable String userEmail) {
        log.info("GET /api/bookings/user/{} - Obteniendo reservas (ADMIN)", userEmail);
        List<BookingResponseDTO> bookings = bookingService.getBookingsByUser(userEmail);
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

    @GetMapping("/search")
    @Operation(summary = "Buscar reservas por película, cine o usuario (solo ADMIN)")
    public ResponseEntity<List<BookingResponseDTO>> searchBookings(@RequestParam(required = false) String q) {
        log.info("Admin buscando reservas: {}", q);
        return ResponseEntity.ok(bookingService.searchBookings(q));
    }
}
