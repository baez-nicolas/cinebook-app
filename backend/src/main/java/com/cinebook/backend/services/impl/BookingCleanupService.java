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
        log.info("Iniciando limpieza de reservas antiguas...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(5);

        List<Booking> bookingsToDelete = bookingRepository.findAll().stream()
                .filter(booking -> {
                    if (booking.getShowtime() == null) return false;
                    return booking.getShowtime().getShowDateTime().isBefore(cutoffTime);
                })
                .toList();

        if (!bookingsToDelete.isEmpty()) {
            bookingRepository.deleteAll(bookingsToDelete);
            log.info("Eliminadas {} reservas antiguas", bookingsToDelete.size());
        } else {
            log.info("No hay reservas antiguas para eliminar");
        }
    }
}

