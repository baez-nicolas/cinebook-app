package com.cinebook.backend.services.impl;

import com.cinebook.backend.entities.Booking;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.repositories.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingCleanupService Tests")
class BookingCleanupServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingCleanupService bookingCleanupService;

    private Booking oldBooking;
    private Booking recentBooking;
    private Booking futureBooking;

    @BeforeEach
    void setUp() {
        Showtime oldShowtime = new Showtime();
        oldShowtime.setShowDateTime(LocalDateTime.now().minusHours(10));

        oldBooking = new Booking();
        oldBooking.setId(1L);
        oldBooking.setShowtime(oldShowtime);

        Showtime recentShowtime = new Showtime();
        recentShowtime.setShowDateTime(LocalDateTime.now().minusHours(3));

        recentBooking = new Booking();
        recentBooking.setId(2L);
        recentBooking.setShowtime(recentShowtime);

        Showtime futureShowtime = new Showtime();
        futureShowtime.setShowDateTime(LocalDateTime.now().plusHours(2));

        futureBooking = new Booking();
        futureBooking.setId(3L);
        futureBooking.setShowtime(futureShowtime);
    }

    @Test
    @DisplayName("cleanupOldBookings - Elimina reservas de funciones finalizadas hace más de 5 horas")
    void cleanupOldBookings_DeletesOldBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(oldBooking, recentBooking, futureBooking));

        bookingCleanupService.cleanupOldBookings();

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("cleanupOldBookings - No elimina reservas de funciones recientes")
    void cleanupOldBookings_DoesNotDeleteRecentBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(recentBooking, futureBooking));

        bookingCleanupService.cleanupOldBookings();

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("cleanupOldBookings - No elimina reservas de funciones futuras")
    void cleanupOldBookings_DoesNotDeleteFutureBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(futureBooking));

        bookingCleanupService.cleanupOldBookings();

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("cleanupOldBookings - No hace nada cuando no hay reservas")
    void cleanupOldBookings_DoesNothingWhenNoBookings() {
        when(bookingRepository.findAll()).thenReturn(new ArrayList<>());

        bookingCleanupService.cleanupOldBookings();

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("cleanupOldBookings - Maneja correctamente reservas con showtime null")
    void cleanupOldBookings_HandlesNullShowtime() {
        Booking bookingWithNullShowtime = new Booking();
        bookingWithNullShowtime.setId(4L);
        bookingWithNullShowtime.setShowtime(null);

        when(bookingRepository.findAll()).thenReturn(Arrays.asList(bookingWithNullShowtime, oldBooking));

        bookingCleanupService.cleanupOldBookings();

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("cleanupOldBookings - Elimina múltiples reservas antiguas")
    void cleanupOldBookings_DeletesMultipleOldBookings() {
        Showtime oldShowtime1 = new Showtime();
        oldShowtime1.setShowDateTime(LocalDateTime.now().minusHours(6));
        Booking oldBooking1 = new Booking();
        oldBooking1.setId(5L);
        oldBooking1.setShowtime(oldShowtime1);

        Showtime oldShowtime2 = new Showtime();
        oldShowtime2.setShowDateTime(LocalDateTime.now().minusHours(8));
        Booking oldBooking2 = new Booking();
        oldBooking2.setId(6L);
        oldBooking2.setShowtime(oldShowtime2);

        when(bookingRepository.findAll()).thenReturn(Arrays.asList(oldBooking1, oldBooking2, recentBooking));

        bookingCleanupService.cleanupOldBookings();

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, times(1)).deleteAll(anyList());
    }
}

