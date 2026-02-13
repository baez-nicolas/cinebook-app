package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.BookingRequestDTO;
import com.cinebook.backend.dtos.BookingResponseDTO;
import com.cinebook.backend.entities.enums.PaymentStatus;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.services.interfaces.IBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingController Tests")
class BookingControllerTest {

    @Mock
    private IBookingService bookingService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingController bookingController;

    private BookingRequestDTO mockRequest;
    private BookingResponseDTO mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = new BookingRequestDTO();
        mockRequest.setShowtimeId(1L);
        mockRequest.setSeatIds(Arrays.asList(1L, 2L, 3L));

        mockResponse = new BookingResponseDTO();
        mockResponse.setBookingId(1L);
        mockResponse.setConfirmationCode("CNB-20260213-0001");
        mockResponse.setUserName("John Doe");
        mockResponse.setMovieTitle("Test Movie");
        mockResponse.setMoviePosterUrl("http://poster.url");
        mockResponse.setCinemaName("Test Cinema");
        mockResponse.setCinemaAddress("Test Address");
        mockResponse.setShowDateTime(LocalDateTime.of(2026, 2, 14, 19, 0));
        mockResponse.setShowtimeType(ShowtimeType.SPANISH_2D);
        mockResponse.setSeatNumbers(Arrays.asList("A1", "A2", "A3"));
        mockResponse.setTotalPrice(new BigDecimal("15000"));
        mockResponse.setPaymentStatus(PaymentStatus.CONFIRMED);
        mockResponse.setBookingDateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("createBooking - Crea reserva exitosamente")
    void createBooking_CreatesSuccessfully() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response = bookingController.createBooking(authentication, mockRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CNB-20260213-0001", response.getBody().getConfirmationCode());
        assertEquals("John Doe", response.getBody().getUserName());

        verify(authentication, times(1)).getName();
        verify(bookingService, times(1)).createBooking(eq("test@example.com"), any(BookingRequestDTO.class));
    }

    @Test
    @DisplayName("createBooking - Usa email del usuario autenticado")
    void createBooking_UsesAuthenticatedUserEmail() {
        when(authentication.getName()).thenReturn("user@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(mockResponse);

        bookingController.createBooking(authentication, mockRequest);

        verify(bookingService, times(1)).createBooking(eq("user@example.com"), any(BookingRequestDTO.class));
    }

    @Test
    @DisplayName("createBooking - Lanza excepción cuando función no existe")
    void createBooking_ThrowsException_WhenShowtimeNotFound() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenThrow(new RuntimeException("Función no encontrada"));

        assertThrows(RuntimeException.class, () -> {
            bookingController.createBooking(authentication, mockRequest);
        });

        verify(bookingService, times(1)).createBooking(anyString(), any(BookingRequestDTO.class));
    }

    @Test
    @DisplayName("createBooking - Lanza excepción cuando asiento no está disponible")
    void createBooking_ThrowsException_WhenSeatNotAvailable() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenThrow(new RuntimeException("El asiento A1 no está disponible"));

        assertThrows(RuntimeException.class, () -> {
            bookingController.createBooking(authentication, mockRequest);
        });
    }

    @Test
    @DisplayName("getAllBookings - Retorna todas las reservas (ADMIN)")
    void getAllBookings_ReturnsAllBookings() {
        List<BookingResponseDTO> bookings = Arrays.asList(mockResponse);
        when(bookingService.getAllBookings()).thenReturn(bookings);

        ResponseEntity<List<BookingResponseDTO>> response = bookingController.getAllBookings();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(bookingService, times(1)).getAllBookings();
    }

    @Test
    @DisplayName("getAllBookings - Retorna lista vacía cuando no hay reservas")
    void getAllBookings_ReturnsEmptyList_WhenNoBookings() {
        when(bookingService.getAllBookings()).thenReturn(Collections.emptyList());

        ResponseEntity<List<BookingResponseDTO>> response = bookingController.getAllBookings();

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(bookingService, times(1)).getAllBookings();
    }

    @Test
    @DisplayName("getMyBookings - Retorna reservas del usuario autenticado")
    void getMyBookings_ReturnsUserBookings() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingsByUser(anyString()))
                .thenReturn(Arrays.asList(mockResponse));

        ResponseEntity<List<BookingResponseDTO>> response = bookingController.getMyBookings(authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(authentication, times(1)).getName();
        verify(bookingService, times(1)).getBookingsByUser("test@example.com");
    }

    @Test
    @DisplayName("getMyBookings - Retorna lista vacía cuando usuario no tiene reservas")
    void getMyBookings_ReturnsEmptyList_WhenUserHasNoBookings() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.getBookingsByUser(anyString()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<BookingResponseDTO>> response = bookingController.getMyBookings(authentication);

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getBookingsByUser - Retorna reservas de usuario específico (ADMIN)")
    void getBookingsByUser_ReturnsUserBookings() {
        when(bookingService.getBookingsByUser("user@example.com"))
                .thenReturn(Arrays.asList(mockResponse));

        ResponseEntity<List<BookingResponseDTO>> response =
                bookingController.getBookingsByUser("user@example.com");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(bookingService, times(1)).getBookingsByUser("user@example.com");
    }

    @Test
    @DisplayName("getBookingByConfirmationCode - Retorna reserva por código")
    void getBookingByConfirmationCode_ReturnsBooking() {
        when(bookingService.getBookingByConfirmationCode("CNB-20260213-0001"))
                .thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response =
                bookingController.getBookingByConfirmationCode("CNB-20260213-0001");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CNB-20260213-0001", response.getBody().getConfirmationCode());

        verify(bookingService, times(1)).getBookingByConfirmationCode("CNB-20260213-0001");
    }

    @Test
    @DisplayName("getBookingByConfirmationCode - Lanza excepción cuando código no existe")
    void getBookingByConfirmationCode_ThrowsException_WhenCodeNotFound() {
        when(bookingService.getBookingByConfirmationCode("INVALID"))
                .thenThrow(new RuntimeException("Reserva no encontrada con código: INVALID"));

        assertThrows(RuntimeException.class, () -> {
            bookingController.getBookingByConfirmationCode("INVALID");
        });
    }

    @Test
    @DisplayName("getBookingById - Retorna reserva por ID")
    void getBookingById_ReturnsBooking() {
        when(bookingService.getBookingById(1L)).thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response = bookingController.getBookingById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getBookingId());

        verify(bookingService, times(1)).getBookingById(1L);
    }

    @Test
    @DisplayName("getBookingById - Lanza excepción cuando ID no existe")
    void getBookingById_ThrowsException_WhenIdNotFound() {
        when(bookingService.getBookingById(999L))
                .thenThrow(new RuntimeException("Reserva no encontrada con ID: 999"));

        assertThrows(RuntimeException.class, () -> {
            bookingController.getBookingById(999L);
        });
    }

    @Test
    @DisplayName("searchBookings - Busca reservas por término (ADMIN)")
    void searchBookings_SearchesBookings() {
        when(bookingService.searchBookings("Test Movie"))
                .thenReturn(Arrays.asList(mockResponse));

        ResponseEntity<List<BookingResponseDTO>> response =
                bookingController.searchBookings("Test Movie");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(bookingService, times(1)).searchBookings("Test Movie");
    }

    @Test
    @DisplayName("searchBookings - Retorna todas si término es null")
    void searchBookings_ReturnsAll_WhenTermIsNull() {
        when(bookingService.searchBookings(null))
                .thenReturn(Arrays.asList(mockResponse));

        ResponseEntity<List<BookingResponseDTO>> response = bookingController.searchBookings(null);

        assertNotNull(response);
        assertEquals(1, response.getBody().size());

        verify(bookingService, times(1)).searchBookings(null);
    }

    @Test
    @DisplayName("createBooking - Retorna información completa de la reserva")
    void createBooking_ReturnsCompleteBookingInfo() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response = bookingController.createBooking(authentication, mockRequest);

        BookingResponseDTO body = response.getBody();
        assertNotNull(body);
        assertAll("Booking info",
            () -> assertNotNull(body.getBookingId()),
            () -> assertNotNull(body.getConfirmationCode()),
            () -> assertNotNull(body.getUserName()),
            () -> assertNotNull(body.getMovieTitle()),
            () -> assertNotNull(body.getCinemaName()),
            () -> assertNotNull(body.getShowDateTime()),
            () -> assertNotNull(body.getSeatNumbers()),
            () -> assertNotNull(body.getTotalPrice()),
            () -> assertNotNull(body.getPaymentStatus())
        );
    }

    @Test
    @DisplayName("createBooking - Calcula precio total correctamente")
    void createBooking_CalculatesTotalPriceCorrectly() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response = bookingController.createBooking(authentication, mockRequest);

        assertEquals(new BigDecimal("15000"), response.getBody().getTotalPrice());
    }

    @Test
    @DisplayName("createBooking - Genera código de confirmación único")
    void createBooking_GeneratesUniqueConfirmationCode() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response = bookingController.createBooking(authentication, mockRequest);

        assertNotNull(response.getBody().getConfirmationCode());
        assertTrue(response.getBody().getConfirmationCode().startsWith("CNB-"));
    }

    @Test
    @DisplayName("getMyBookings - Solo retorna reservas del usuario autenticado")
    void getMyBookings_OnlyReturnsAuthenticatedUserBookings() {
        when(authentication.getName()).thenReturn("user1@example.com");
        when(bookingService.getBookingsByUser("user1@example.com"))
                .thenReturn(Arrays.asList(mockResponse));

        bookingController.getMyBookings(authentication);

        verify(bookingService, times(1)).getBookingsByUser("user1@example.com");
        verify(bookingService, never()).getAllBookings();
    }

    @Test
    @DisplayName("searchBookings - Busca en película, cine y usuario")
    void searchBookings_SearchesInMultipleFields() {
        BookingResponseDTO booking1 = new BookingResponseDTO();
        booking1.setMovieTitle("Action Movie");

        BookingResponseDTO booking2 = new BookingResponseDTO();
        booking2.setCinemaName("Cinema Test");

        when(bookingService.searchBookings("Test"))
                .thenReturn(Arrays.asList(booking1, booking2));

        ResponseEntity<List<BookingResponseDTO>> response = bookingController.searchBookings("Test");

        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("createBooking - Reserva múltiples asientos correctamente")
    void createBooking_ReservesMultipleSeats() {
        mockRequest.setSeatIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        mockResponse.setSeatNumbers(Arrays.asList("A1", "A2", "A3", "A4", "A5"));

        when(authentication.getName()).thenReturn("test@example.com");
        when(bookingService.createBooking(anyString(), any(BookingRequestDTO.class)))
                .thenReturn(mockResponse);

        ResponseEntity<BookingResponseDTO> response = bookingController.createBooking(authentication, mockRequest);

        assertEquals(5, response.getBody().getSeatNumbers().size());
    }
}

