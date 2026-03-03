package com.cinebook.backend.services.impl;

import com.cinebook.backend.entities.Booking;
import com.cinebook.backend.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupService {

    private final BookingRepository bookingRepository;

    @Scheduled(cron = "0 0 0,12 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void cleanupOldBookings() {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  LIMPIEZA DE RESERVAS ANTIGUAS                         ║");
        log.info("╚══════════════════════════════════════════════════════════╝");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(5);

        List<Booking> bookings = bookingRepository.findAll();

        List<Booking> bookingsToDelete = bookings.stream()
                .filter(booking -> booking.getShowtime() != null)
                .filter(booking -> booking.getShowtime().getShowDateTime().isBefore(cutoffTime))
                .toList();

        if (!bookingsToDelete.isEmpty()) {
            bookingRepository.deleteAll(bookingsToDelete);
            log.info("Eliminadas {} reservas de funciones finalizadas hace más de 5 horas",
                    bookingsToDelete.size());
        } else {
            log.info("No hay reservas antiguas para eliminar");
        }

        log.info("═══════════════════════════════════════════════════════════");
    }
}

