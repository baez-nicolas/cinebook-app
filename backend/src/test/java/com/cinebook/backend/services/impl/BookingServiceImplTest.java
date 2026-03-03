package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.BookingRequestDTO;
import com.cinebook.backend.dtos.BookingResponseDTO;
import com.cinebook.backend.entities.*;
import com.cinebook.backend.entities.enums.PaymentStatus;
import com.cinebook.backend.entities.enums.SeatStatus;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.repositories.UserRepository;
import com.cinebook.backend.services.interfaces.ISeatService;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingServiceImpl Tests")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ISeatService seatService;

    @Mock
    private IWeeklyScheduleService weeklyScheduleService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User mockUser;
    private Showtime mockShowtime;
    private Movie mockMovie;
    private Cinema mockCinema;
    private List<Seat> mockSeats;
    private BookingRequestDTO bookingRequest;
    private Booking mockBooking;
    private WeeklySchedule mockWeeklySchedule;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        mockMovie = new Movie();
        mockMovie.setId(1L);
        mockMovie.setTitle("Test Movie");
        mockMovie.setPosterUrl("http://poster.url");

        mockCinema = new Cinema();
        mockCinema.setId(1L);
        mockCinema.setName("Test Cinema");
        mockCinema.setAddress("Test Address");

        mockShowtime = new Showtime();
        mockShowtime.setId(1L);
        mockShowtime.setMovie(mockMovie);
        mockShowtime.setCinema(mockCinema);
        mockShowtime.setShowDateTime(LocalDateTime.now().plusDays(1));
        mockShowtime.setPrice(new BigDecimal("5000"));
        mockShowtime.setType(ShowtimeType.SPANISH_2D);

        mockSeats = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Seat seat = new Seat();
            seat.setId((long) i);
            seat.setSeatNumber("A" + i);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setShowtime(mockShowtime);
            mockSeats.add(seat);
        }

        bookingRequest = new BookingRequestDTO();
        bookingRequest.setShowtimeId(1L);
        bookingRequest.setSeatIds(Arrays.asList(1L, 2L, 3L));

        mockWeeklySchedule = new WeeklySchedule();
        mockWeeklySchedule.setWeekId(1L);

        mockBooking = new Booking();
        mockBooking.setId(1L);
        mockBooking.setUser(mockUser);
        mockBooking.setShowtime(mockShowtime);
        mockBooking.setSeats(mockSeats);
        mockBooking.setTotalPrice(new BigDecimal("15000"));
        mockBooking.setPaymentStatus(PaymentStatus.CONFIRMED);
        mockBooking.setBookingDateTime(LocalDateTime.now());
        mockBooking.setWeekId(1L);
        mockBooking.setConfirmationCode("CNB-20260213-0001");
    }

    @Test
    @DisplayName("createBooking - Reserva exitosa con datos válidos")
    void createBooking_ValidData_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime));
        when(seatRepository.findAllById(bookingRequest.getSeatIds())).thenReturn(mockSeats);
        when(weeklyScheduleService.getCurrentWeek()).thenReturn(mockWeeklySchedule);
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockBooking);
        doNothing().when(seatService).reserveSeats(anyList());

        BookingResponseDTO result = bookingService.createBooking("test@example.com", bookingRequest);

        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals("CNB-20260213-0001", result.getConfirmationCode());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Test Movie", result.getMovieTitle());
        assertEquals(3, result.getSeatNumbers().size());
        assertEquals(new BigDecimal("15000"), result.getTotalPrice());
        assertEquals(PaymentStatus.CONFIRMED, result.getPaymentStatus());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(showtimeRepository, times(1)).findById(1L);
        verify(seatRepository, times(1)).findAllById(bookingRequest.getSeatIds());
        verify(seatService, times(1)).reserveSeats(bookingRequest.getSeatIds());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking - Usuario no existe, lanza excepción")
    void createBooking_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("Usuario no encontrado con email: test@example.com", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(showtimeRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking - Función no existe, lanza excepción")
    void createBooking_ShowtimeNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("Función no encontrada con ID: 1", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(showtimeRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking - Función ya pasó, lanza excepción")
    void createBooking_ShowtimeInPast_ThrowsException() {
        mockShowtime.setShowDateTime(LocalDateTime.now().minusHours(1));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("No se pueden reservar funciones que ya comenzaron o están en curso", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(showtimeRepository, times(1)).findById(1L);
        verify(seatRepository, never()).findAllById(anyList());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking - Algunos asientos no encontrados, lanza excepción")
    void createBooking_SeatsNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime));
        when(seatRepository.findAllById(bookingRequest.getSeatIds())).thenReturn(Arrays.asList(mockSeats.get(0), mockSeats.get(1)));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("Algunos asientos no fueron encontrados", exception.getMessage());

        verify(seatRepository, times(1)).findAllById(bookingRequest.getSeatIds());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking - Asiento no disponible, lanza excepción")
    void createBooking_SeatNotAvailable_ThrowsException() {
        mockSeats.get(1).setStatus(SeatStatus.RESERVED_USER);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime));
        when(seatRepository.findAllById(bookingRequest.getSeatIds())).thenReturn(mockSeats);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking("test@example.com", bookingRequest);
        });

        assertEquals("El asiento A2 no está disponible", exception.getMessage());

        verify(seatRepository, times(1)).findAllById(bookingRequest.getSeatIds());
        verify(seatService, never()).reserveSeats(anyList());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("createBooking - Precio total se calcula correctamente")
    void createBooking_TotalPriceCalculatedCorrectly() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime));
        when(seatRepository.findAllById(bookingRequest.getSeatIds())).thenReturn(mockSeats);
        when(weeklyScheduleService.getCurrentWeek()).thenReturn(mockWeeklySchedule);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            assertEquals(new BigDecimal("15000"), savedBooking.getTotalPrice());
            return mockBooking;
        });
        doNothing().when(seatService).reserveSeats(anyList());

        bookingService.createBooking("test@example.com", bookingRequest);

        verify(bookingRepository, times(1)).save(argThat(booking ->
                booking.getTotalPrice().equals(new BigDecimal("15000"))
        ));
    }

    @Test
    @DisplayName("getBookingsByUser - Retorna reservas del usuario")
    void getBookingsByUser_ValidEmail_ReturnsBookings() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(bookingRepository.findByUserId(1L)).thenReturn(Arrays.asList(mockBooking));

        List<BookingResponseDTO> result = bookingService.getBookingsByUser("test@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CNB-20260213-0001", result.get(0).getConfirmationCode());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(bookingRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("getBookingsByUser - Usuario no existe, lanza excepción")
    void getBookingsByUser_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.getBookingsByUser("test@example.com");
        });

        assertEquals("Usuario no encontrado con email: test@example.com", exception.getMessage());

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(bookingRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("getAllBookings - Retorna todas las reservas")
    void getAllBookings_ReturnsAllBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(mockBooking));

        List<BookingResponseDTO> result = bookingService.getAllBookings();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getBookingByConfirmationCode - Reserva encontrada, retorna DTO")
    void getBookingByConfirmationCode_Found_ReturnsDTO() {
        when(bookingRepository.findByConfirmationCode("CNB-20260213-0001")).thenReturn(Optional.of(mockBooking));

        BookingResponseDTO result = bookingService.getBookingByConfirmationCode("CNB-20260213-0001");

        assertNotNull(result);
        assertEquals("CNB-20260213-0001", result.getConfirmationCode());

        verify(bookingRepository, times(1)).findByConfirmationCode("CNB-20260213-0001");
    }

    @Test
    @DisplayName("getBookingByConfirmationCode - No encontrada, lanza excepción")
    void getBookingByConfirmationCode_NotFound_ThrowsException() {
        when(bookingRepository.findByConfirmationCode("INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.getBookingByConfirmationCode("INVALID");
        });

        assertEquals("Reserva no encontrada con código: INVALID", exception.getMessage());

        verify(bookingRepository, times(1)).findByConfirmationCode("INVALID");
    }

    @Test
    @DisplayName("getBookingById - Reserva encontrada, retorna DTO")
    void getBookingById_Found_ReturnsDTO() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        BookingResponseDTO result = bookingService.getBookingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getBookingId());

        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getBookingById - No encontrada, lanza excepción")
    void getBookingById_NotFound_ThrowsException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.getBookingById(999L);
        });

        assertEquals("Reserva no encontrada con ID: 999", exception.getMessage());

        verify(bookingRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("generateConfirmationCode - Genera código con formato correcto")
    void generateConfirmationCode_GeneratesCorrectFormat() {
        when(bookingRepository.count()).thenReturn(5L);

        String code = bookingService.generateConfirmationCode();

        assertNotNull(code);
        assertTrue(code.startsWith("CNB-"));
        assertTrue(code.matches("CNB-\\d{8}-\\d{4}"));

        verify(bookingRepository, times(1)).count();
    }

    @Test
    @DisplayName("searchBookings - Con término válido, retorna resultados")
    void searchBookings_ValidTerm_ReturnsResults() {
        when(bookingRepository.searchBookings("Test")).thenReturn(Arrays.asList(mockBooking));

        List<BookingResponseDTO> result = bookingService.searchBookings("Test");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bookingRepository, times(1)).searchBookings("Test");
    }

    @Test
    @DisplayName("searchBookings - Término vacío, retorna todas las reservas")
    void searchBookings_EmptyTerm_ReturnsAllBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(mockBooking));

        List<BookingResponseDTO> result = bookingService.searchBookings("");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, never()).searchBookings(anyString());
    }

    @Test
    @DisplayName("searchBookings - Término null, retorna todas las reservas")
    void searchBookings_NullTerm_ReturnsAllBookings() {
        when(bookingRepository.findAll()).thenReturn(Arrays.asList(mockBooking));

        List<BookingResponseDTO> result = bookingService.searchBookings(null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(bookingRepository, times(1)).findAll();
        verify(bookingRepository, never()).searchBookings(anyString());
    }

    @Test
    @DisplayName("convertToDTO - Convierte correctamente todos los campos")
    void convertToDTO_ConvertsAllFieldsCorrectly() {
        BookingResponseDTO result = bookingService.convertToDTO(mockBooking);

        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals("CNB-20260213-0001", result.getConfirmationCode());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Test Movie", result.getMovieTitle());
        assertEquals("http://poster.url", result.getMoviePosterUrl());
        assertEquals("Test Cinema", result.getCinemaName());
        assertEquals("Test Address", result.getCinemaAddress());
        assertEquals(ShowtimeType.SPANISH_2D, result.getShowtimeType());
        assertEquals(3, result.getSeatNumbers().size());
        assertEquals(new BigDecimal("15000"), result.getTotalPrice());
        assertEquals(PaymentStatus.CONFIRMED, result.getPaymentStatus());
    }

    @Test
    @DisplayName("convertToDTO - Maneja correctamente showtime null después de cascade delete")
    void convertToDTO_HandlesNullShowtime() {
        mockBooking.setShowtime(null);

        BookingResponseDTO result = bookingService.convertToDTO(mockBooking);

        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals("CNB-20260213-0001", result.getConfirmationCode());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Función eliminada", result.getMovieTitle());
        assertNull(result.getMoviePosterUrl());
        assertEquals("N/A", result.getCinemaName());
        assertEquals("N/A", result.getCinemaAddress());
        assertNull(result.getShowDateTime());
        assertNull(result.getShowtimeType());
        assertEquals(3, result.getSeatNumbers().size());
        assertEquals(new BigDecimal("15000"), result.getTotalPrice());
        assertEquals(PaymentStatus.CONFIRMED, result.getPaymentStatus());
    }
}

