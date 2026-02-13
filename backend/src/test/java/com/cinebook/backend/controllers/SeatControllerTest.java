package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.SeatDTO;
import com.cinebook.backend.entities.enums.SeatStatus;
import com.cinebook.backend.services.interfaces.ISeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatController Tests")
class SeatControllerTest {

    @Mock
    private ISeatService seatService;

    @InjectMocks
    private SeatController seatController;

    private SeatDTO availableSeat1;
    private SeatDTO availableSeat2;
    private SeatDTO reservedSeat;
    private SeatDTO reservedRandomSeat;

    @BeforeEach
    void setUp() {
        availableSeat1 = new SeatDTO();
        availableSeat1.setId(1L);
        availableSeat1.setSeatNumber("A1");
        availableSeat1.setStatus(SeatStatus.AVAILABLE);

        availableSeat2 = new SeatDTO();
        availableSeat2.setId(2L);
        availableSeat2.setSeatNumber("A2");
        availableSeat2.setStatus(SeatStatus.AVAILABLE);

        reservedSeat = new SeatDTO();
        reservedSeat.setId(3L);
        reservedSeat.setSeatNumber("A3");
        reservedSeat.setStatus(SeatStatus.RESERVED_USER);

        reservedRandomSeat = new SeatDTO();
        reservedRandomSeat.setId(4L);
        reservedRandomSeat.setSeatNumber("A4");
        reservedRandomSeat.setStatus(SeatStatus.RESERVED_RANDOM);
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna todos los asientos de una función")
    void getSeatsByShowtime_ReturnsAllSeats() {
        when(seatService.getSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, availableSeat2, reservedSeat, reservedRandomSeat));

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());

        verify(seatService, times(1)).getSeatsByShowtime(1L);
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna 120 asientos por función")
    void getSeatsByShowtime_Returns120Seats() {
        List<SeatDTO> seats = generateSeats(120);
        when(seatService.getSeatsByShowtime(1L)).thenReturn(seats);

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        assertEquals(120, response.getBody().size());
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna lista vacía cuando no hay asientos")
    void getSeatsByShowtime_ReturnsEmptyList_WhenNoSeats() {
        when(seatService.getSeatsByShowtime(999L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(999L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getSeatsByShowtime - Incluye asientos disponibles y reservados")
    void getSeatsByShowtime_IncludesAvailableAndReservedSeats() {
        when(seatService.getSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, reservedSeat));

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        assertTrue(response.getBody().stream().anyMatch(s -> s.getStatus() == SeatStatus.AVAILABLE));
        assertTrue(response.getBody().stream().anyMatch(s -> s.getStatus() == SeatStatus.RESERVED_USER));
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna información completa de los asientos")
    void getSeatsByShowtime_ReturnsCompleteInfo() {
        when(seatService.getSeatsByShowtime(1L)).thenReturn(Arrays.asList(availableSeat1));

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        SeatDTO seat = response.getBody().get(0);
        assertAll("Seat info",
            () -> assertNotNull(seat.getId()),
            () -> assertNotNull(seat.getSeatNumber()),
            () -> assertNotNull(seat.getStatus())
        );
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Solo retorna asientos disponibles")
    void getAvailableSeatsByShowtime_OnlyReturnsAvailableSeats() {
        when(seatService.getAvailableSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, availableSeat2));

        ResponseEntity<List<SeatDTO>> response = seatController.getAvailableSeatsByShowtime(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(s -> s.getStatus() == SeatStatus.AVAILABLE));

        verify(seatService, times(1)).getAvailableSeatsByShowtime(1L);
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - No incluye asientos reservados")
    void getAvailableSeatsByShowtime_DoesNotIncludeReservedSeats() {
        when(seatService.getAvailableSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, availableSeat2));

        ResponseEntity<List<SeatDTO>> response = seatController.getAvailableSeatsByShowtime(1L);

        assertFalse(response.getBody().stream().anyMatch(s -> s.getStatus() == SeatStatus.RESERVED_USER));
        assertFalse(response.getBody().stream().anyMatch(s -> s.getStatus() == SeatStatus.RESERVED_RANDOM));
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Retorna lista vacía cuando no hay asientos disponibles")
    void getAvailableSeatsByShowtime_ReturnsEmptyList_WhenNoAvailableSeats() {
        when(seatService.getAvailableSeatsByShowtime(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<SeatDTO>> response = seatController.getAvailableSeatsByShowtime(1L);

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Retorna asientos con numeración correcta")
    void getAvailableSeatsByShowtime_ReturnsCorrectSeatNumbers() {
        when(seatService.getAvailableSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, availableSeat2));

        ResponseEntity<List<SeatDTO>> response = seatController.getAvailableSeatsByShowtime(1L);

        List<SeatDTO> seats = response.getBody();
        assertEquals("A1", seats.get(0).getSeatNumber());
        assertEquals("A2", seats.get(1).getSeatNumber());
    }

    @Test
    @DisplayName("getSeatsByShowtime - Verifica llamada única al servicio")
    void getSeatsByShowtime_CallsServiceOnce() {
        when(seatService.getSeatsByShowtime(1L)).thenReturn(Arrays.asList(availableSeat1));

        seatController.getSeatsByShowtime(1L);

        verify(seatService, times(1)).getSeatsByShowtime(1L);
        verifyNoMoreInteractions(seatService);
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Verifica llamada única al servicio")
    void getAvailableSeatsByShowtime_CallsServiceOnce() {
        when(seatService.getAvailableSeatsByShowtime(1L)).thenReturn(Arrays.asList(availableSeat1));

        seatController.getAvailableSeatsByShowtime(1L);

        verify(seatService, times(1)).getAvailableSeatsByShowtime(1L);
        verifyNoMoreInteractions(seatService);
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna asientos de diferentes estados")
    void getSeatsByShowtime_ReturnsDifferentStatuses() {
        when(seatService.getSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, reservedSeat, reservedRandomSeat));

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        List<SeatDTO> seats = response.getBody();
        assertTrue(seats.stream().anyMatch(s -> s.getStatus() == SeatStatus.AVAILABLE));
        assertTrue(seats.stream().anyMatch(s -> s.getStatus() == SeatStatus.RESERVED_USER));
        assertTrue(seats.stream().anyMatch(s -> s.getStatus() == SeatStatus.RESERVED_RANDOM));
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna asientos con IDs únicos")
    void getSeatsByShowtime_ReturnsUniqueIds() {
        when(seatService.getSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(availableSeat1, availableSeat2));

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        List<Long> ids = response.getBody().stream().map(SeatDTO::getId).toList();
        assertEquals(2, ids.stream().distinct().count());
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Solo cuenta asientos disponibles")
    void getAvailableSeatsByShowtime_OnlyCountsAvailable() {
        List<SeatDTO> availableSeats = Arrays.asList(availableSeat1, availableSeat2);
        when(seatService.getAvailableSeatsByShowtime(1L)).thenReturn(availableSeats);

        ResponseEntity<List<SeatDTO>> response = seatController.getAvailableSeatsByShowtime(1L);

        long availableCount = response.getBody().stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .count();

        assertEquals(2, availableCount);
        assertEquals(response.getBody().size(), availableCount);
    }

    @Test
    @DisplayName("getSeatsByShowtime - Retorna asientos en orden correcto")
    void getSeatsByShowtime_MaintainsOrder() {
        SeatDTO seatA1 = new SeatDTO(1L, "A1", SeatStatus.AVAILABLE);
        SeatDTO seatA2 = new SeatDTO(2L, "A2", SeatStatus.AVAILABLE);
        SeatDTO seatB1 = new SeatDTO(3L, "B1", SeatStatus.AVAILABLE);

        when(seatService.getSeatsByShowtime(1L))
                .thenReturn(Arrays.asList(seatA1, seatA2, seatB1));

        ResponseEntity<List<SeatDTO>> response = seatController.getSeatsByShowtime(1L);

        List<String> seatNumbers = response.getBody().stream()
                .map(SeatDTO::getSeatNumber)
                .toList();

        assertEquals("A1", seatNumbers.get(0));
        assertEquals("A2", seatNumbers.get(1));
        assertEquals("B1", seatNumbers.get(2));
    }

    @Test
    @DisplayName("getSeatsByShowtime - Funciona con diferentes funciones")
    void getSeatsByShowtime_WorksWithDifferentShowtimes() {
        when(seatService.getSeatsByShowtime(1L)).thenReturn(Arrays.asList(availableSeat1));
        when(seatService.getSeatsByShowtime(2L)).thenReturn(Arrays.asList(availableSeat2));

        ResponseEntity<List<SeatDTO>> response1 = seatController.getSeatsByShowtime(1L);
        ResponseEntity<List<SeatDTO>> response2 = seatController.getSeatsByShowtime(2L);

        assertEquals("A1", response1.getBody().get(0).getSeatNumber());
        assertEquals("A2", response2.getBody().get(0).getSeatNumber());

        verify(seatService, times(1)).getSeatsByShowtime(1L);
        verify(seatService, times(1)).getSeatsByShowtime(2L);
    }

    @Test
    @DisplayName("getAvailableSeatsByShowtime - Funciona con diferentes funciones")
    void getAvailableSeatsByShowtime_WorksWithDifferentShowtimes() {
        when(seatService.getAvailableSeatsByShowtime(1L)).thenReturn(Arrays.asList(availableSeat1));
        when(seatService.getAvailableSeatsByShowtime(2L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<SeatDTO>> response1 = seatController.getAvailableSeatsByShowtime(1L);
        ResponseEntity<List<SeatDTO>> response2 = seatController.getAvailableSeatsByShowtime(2L);

        assertEquals(1, response1.getBody().size());
        assertEquals(0, response2.getBody().size());
    }

    private List<SeatDTO> generateSeats(int count) {
        return java.util.stream.IntStream.range(1, count + 1)
                .mapToObj(i -> new SeatDTO((long) i, "SEAT" + i, SeatStatus.AVAILABLE))
                .toList();
    }
}

