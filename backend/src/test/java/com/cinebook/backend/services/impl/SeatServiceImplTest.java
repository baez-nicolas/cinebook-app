package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.SeatDTO;
import com.cinebook.backend.entities.*;
import com.cinebook.backend.entities.enums.SeatStatus;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatServiceImpl Tests")
class SeatServiceImplTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private IWeeklyScheduleService weeklyScheduleService;

    private SeatServiceImpl seatService;

    private Showtime mockShowtime;
    private Movie mockMovie;
    private Cinema mockCinema;
    private WeeklySchedule mockWeeklySchedule;
    private List<Seat> mockSeats;

    @BeforeEach
    void setUp() {
        seatService = new SeatServiceImpl(
                seatRepository,
                showtimeRepository,
                weeklyScheduleService
        );

        mockMovie = new Movie();
        mockMovie.setId(1L);
        mockMovie.setTitle("Test Movie");

        mockCinema = new Cinema();
        mockCinema.setId(1L);
        mockCinema.setName("Test Cinema");

        mockShowtime = new Showtime();
        mockShowtime.setId(1L);
        mockShowtime.setMovie(mockMovie);
        mockShowtime.setCinema(mockCinema);
        mockShowtime.setAvailableSeats(120);

        mockWeeklySchedule = new WeeklySchedule();
        mockWeeklySchedule.setWeekId(1L);

        mockSeats = Arrays.asList(
                createSeat(1L, "A1", SeatStatus.AVAILABLE, mockShowtime),
                createSeat(2L, "A2", SeatStatus.AVAILABLE, mockShowtime),
                createSeat(3L, "A3", SeatStatus.RESERVED_USER, mockShowtime)
        );
    }

    private Seat createSeat(Long id, String seatNumber, SeatStatus status, Showtime showtime) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setSeatNumber(seatNumber);
        seat.setStatus(status);
        seat.setShowtime(showtime);
        seat.setWeekId(1L);
        return seat;
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna todos los asientos de una función")
    void getSeatsByShowtime_ReturnsAllSeats() {
        when(seatRepository.findByShowtimeId(1L)).thenReturn(mockSeats);

        List<SeatDTO> result = seatService.getSeatsByShowtime(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("A1", result.get(0).getSeatNumber());
        assertEquals("A2", result.get(1).getSeatNumber());
        assertEquals("A3", result.get(2).getSeatNumber());

        verify(seatRepository, times(1)).findByShowtimeId(1L);
    }

    @Test
    @DisplayName("getSeatsByShowtime - Lista vacía cuando no hay asientos")
    void getSeatsByShowtime_EmptyList_WhenNoSeats() {
        when(seatRepository.findByShowtimeId(999L)).thenReturn(Collections.emptyList());

        List<SeatDTO> result = seatService.getSeatsByShowtime(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(seatRepository, times(1)).findByShowtimeId(999L);
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Retorna solo asientos disponibles")
    void getAvailableSeatsByShowtime_ReturnsOnlyAvailableSeats() {
        List<Seat> availableSeats = Arrays.asList(mockSeats.get(0), mockSeats.get(1));
        when(seatRepository.findByShowtimeIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(availableSeats);

        List<SeatDTO> result = seatService.getAvailableSeatsByShowtime(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(SeatStatus.AVAILABLE, result.get(0).getStatus());
        assertEquals(SeatStatus.AVAILABLE, result.get(1).getStatus());

        verify(seatRepository, times(1)).findByShowtimeIdAndStatus(1L, SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Lista vacía cuando no hay disponibles")
    void getAvailableSeatsByShowtime_EmptyList_WhenNoAvailable() {
        when(seatRepository.findByShowtimeIdAndStatus(1L, SeatStatus.AVAILABLE)).thenReturn(Collections.emptyList());

        List<SeatDTO> result = seatService.getAvailableSeatsByShowtime(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(seatRepository, times(1)).findByShowtimeIdAndStatus(1L, SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("generateSeatsForShowtime - Genera 120 asientos correctamente")
    void generateSeatsForShowtime_Generates120Seats() {
        mockShowtime.setWeekId(1L);

        when(seatRepository.findByShowtimeId(1L)).thenReturn(Collections.emptyList());
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime));
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(mockShowtime);

        seatService.generateSeatsForShowtime(mockShowtime);

        verify(seatRepository, atLeast(2)).saveAll(anyList());
        verify(showtimeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("generateSeatsForShowtime - No genera si ya existen asientos")
    void generateSeatsForShowtime_DoesNotGenerate_WhenSeatsExist() {
        when(seatRepository.findByShowtimeId(1L)).thenReturn(mockSeats);

        seatService.generateSeatsForShowtime(mockShowtime);

        verify(seatRepository, times(1)).findByShowtimeId(1L);
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("generateSeatsForAllShowtimes - Genera asientos para todas las funciones")
    void generateSeatsForAllShowtimes_GeneratesForAllShowtimes() {
        mockShowtime.setWeekId(1L);

        Showtime showtime2 = new Showtime();
        showtime2.setId(2L);
        showtime2.setMovie(mockMovie);
        showtime2.setCinema(mockCinema);
        showtime2.setAvailableSeats(120);
        showtime2.setWeekId(1L);

        List<Showtime> showtimes = Arrays.asList(mockShowtime, showtime2);

        when(weeklyScheduleService.getCurrentWeek()).thenReturn(mockWeeklySchedule);
        when(showtimeRepository.findByWeekId(1L)).thenReturn(showtimes);
        when(seatRepository.findByShowtimeId(anyLong())).thenReturn(Collections.emptyList());
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(showtimeRepository.findById(anyLong())).thenReturn(Optional.of(mockShowtime));
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(mockShowtime);

        seatService.generateSeatsForAllShowtimes();

        verify(weeklyScheduleService, times(1)).getCurrentWeek();
        verify(showtimeRepository, times(1)).findByWeekId(1L);
        verify(seatRepository, atLeast(2)).findByShowtimeId(anyLong());
    }

    @Test
    @DisplayName("generateSeatsForAllShowtimes - No genera cuando no hay funciones")
    void generateSeatsForAllShowtimes_DoesNotGenerate_WhenNoShowtimes() {
        when(weeklyScheduleService.getCurrentWeek()).thenReturn(mockWeeklySchedule);
        when(showtimeRepository.findByWeekId(1L)).thenReturn(Collections.emptyList());

        seatService.generateSeatsForAllShowtimes();

        verify(weeklyScheduleService, times(1)).getCurrentWeek();
        verify(showtimeRepository, times(1)).findByWeekId(1L);
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("reserveSeats - Reserva asientos disponibles exitosamente")
    void reserveSeats_Success_WithAvailableSeats() {
        List<Long> seatIds = Arrays.asList(1L, 2L);
        List<Seat> availableSeats = Arrays.asList(mockSeats.get(0), mockSeats.get(1));

        when(seatRepository.findAllById(seatIds)).thenReturn(availableSeats);
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(showtimeRepository.save(any(Showtime.class))).thenReturn(mockShowtime);

        seatService.reserveSeats(seatIds);

        verify(seatRepository, times(1)).findAllById(seatIds);
        verify(seatRepository, times(1)).saveAll(anyList());
        verify(showtimeRepository, times(1)).save(any(Showtime.class));

        assertEquals(SeatStatus.RESERVED_USER, availableSeats.get(0).getStatus());
        assertEquals(SeatStatus.RESERVED_USER, availableSeats.get(1).getStatus());
    }

    @Test
    @DisplayName("reserveSeats - Lanza excepción con lista vacía")
    void reserveSeats_ThrowsException_WithEmptyList() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            seatService.reserveSeats(Collections.emptyList());
        });

        assertEquals("Debe seleccionar al menos un asiento", exception.getMessage());

        verify(seatRepository, never()).findAllById(anyList());
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("reserveSeats - Lanza excepción con lista null")
    void reserveSeats_ThrowsException_WithNullList() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            seatService.reserveSeats(null);
        });

        assertEquals("Debe seleccionar al menos un asiento", exception.getMessage());

        verify(seatRepository, never()).findAllById(anyList());
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("reserveSeats - Lanza excepción cuando asientos no encontrados")
    void reserveSeats_ThrowsException_WhenSeatsNotFound() {
        List<Long> seatIds = Arrays.asList(1L, 2L, 999L);
        List<Seat> foundSeats = Arrays.asList(mockSeats.get(0), mockSeats.get(1));

        when(seatRepository.findAllById(seatIds)).thenReturn(foundSeats);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            seatService.reserveSeats(seatIds);
        });

        assertEquals("Algunos asientos no fueron encontrados", exception.getMessage());

        verify(seatRepository, times(1)).findAllById(seatIds);
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("reserveSeats - Lanza excepción cuando asiento no disponible")
    void reserveSeats_ThrowsException_WhenSeatNotAvailable() {
        List<Long> seatIds = Arrays.asList(1L, 3L);
        List<Seat> seats = Arrays.asList(mockSeats.get(0), mockSeats.get(2));

        when(seatRepository.findAllById(seatIds)).thenReturn(seats);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            seatService.reserveSeats(seatIds);
        });

        assertEquals("El asiento A3 no está disponible", exception.getMessage());

        verify(seatRepository, times(1)).findAllById(seatIds);
        verify(seatRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("reserveSeats - Actualiza availableSeats en showtime")
    void reserveSeats_UpdatesAvailableSeatsInShowtime() {
        List<Long> seatIds = Arrays.asList(1L, 2L);
        List<Seat> availableSeats = Arrays.asList(mockSeats.get(0), mockSeats.get(1));

        when(seatRepository.findAllById(seatIds)).thenReturn(availableSeats);
        when(seatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(invocation -> {
            Showtime saved = invocation.getArgument(0);
            assertEquals(118, saved.getAvailableSeats());
            return saved;
        });

        seatService.reserveSeats(seatIds);

        verify(showtimeRepository, times(1)).save(argThat(showtime ->
                showtime.getAvailableSeats() == 118
        ));
    }

    @Test
    @DisplayName("convertToDTO - Convierte todos los campos correctamente")
    void convertToDTO_ConvertsAllFieldsCorrectly() {
        Seat seat = mockSeats.get(0);

        SeatDTO result = seatService.convertToDTO(seat);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("A1", result.getSeatNumber());
        assertEquals(SeatStatus.AVAILABLE, result.getStatus());
    }

    @Test
    @DisplayName("convertToDTO - Maneja diferentes estados correctamente")
    void convertToDTO_HandlesDifferentStatuses() {
        Seat availableSeat = createSeat(10L, "B1", SeatStatus.AVAILABLE, mockShowtime);
        Seat reservedUserSeat = createSeat(11L, "B2", SeatStatus.RESERVED_USER, mockShowtime);
        Seat reservedRandomSeat = createSeat(12L, "B3", SeatStatus.RESERVED_RANDOM, mockShowtime);

        SeatDTO dto1 = seatService.convertToDTO(availableSeat);
        SeatDTO dto2 = seatService.convertToDTO(reservedUserSeat);
        SeatDTO dto3 = seatService.convertToDTO(reservedRandomSeat);

        assertEquals(SeatStatus.AVAILABLE, dto1.getStatus());
        assertEquals(SeatStatus.RESERVED_USER, dto2.getStatus());
        assertEquals(SeatStatus.RESERVED_RANDOM, dto3.getStatus());
    }

    @Test
    @DisplayName("getSeatsByShowtime - Convierte correctamente a DTO")
    void getSeatsByShowtime_ConvertsToDTO_Correctly() {
        when(seatRepository.findByShowtimeId(1L)).thenReturn(mockSeats);

        List<SeatDTO> result = seatService.getSeatsByShowtime(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertAll(
                () -> assertEquals(1L, result.get(0).getId()),
                () -> assertEquals("A1", result.get(0).getSeatNumber()),
                () -> assertEquals(SeatStatus.AVAILABLE, result.get(0).getStatus()),
                () -> assertEquals(2L, result.get(1).getId()),
                () -> assertEquals("A2", result.get(1).getSeatNumber()),
                () -> assertEquals(SeatStatus.AVAILABLE, result.get(1).getStatus()),
                () -> assertEquals(3L, result.get(2).getId()),
                () -> assertEquals("A3", result.get(2).getSeatNumber()),
                () -> assertEquals(SeatStatus.RESERVED_USER, result.get(2).getStatus())
        );
    }
}

