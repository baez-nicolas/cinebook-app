package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.ShowtimeDTO;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeController Tests")
class ShowtimeControllerTest {

    @Mock
    private IShowtimeService showtimeService;

    @InjectMocks
    private ShowtimeController showtimeController;

    private ShowtimeDTO mockShowtimeDTO1;
    private ShowtimeDTO mockShowtimeDTO2;

    @BeforeEach
    void setUp() {
        mockShowtimeDTO1 = new ShowtimeDTO();
        mockShowtimeDTO1.setId(1L);
        mockShowtimeDTO1.setMovieId(1L);
        mockShowtimeDTO1.setMovieTitle("Test Movie 1");
        mockShowtimeDTO1.setMoviePosterUrl("http://poster1.url");
        mockShowtimeDTO1.setCinemaId(1L);
        mockShowtimeDTO1.setCinemaName("Test Cinema");
        mockShowtimeDTO1.setShowDateTime(LocalDateTime.now().plusDays(1).withHour(17).withMinute(30));
        mockShowtimeDTO1.setType(ShowtimeType.SPANISH_2D);
        mockShowtimeDTO1.setPrice(new BigDecimal("5000"));
        mockShowtimeDTO1.setAvailableSeats(100);
        mockShowtimeDTO1.setTotalSeats(120);

        mockShowtimeDTO2 = new ShowtimeDTO();
        mockShowtimeDTO2.setId(2L);
        mockShowtimeDTO2.setMovieId(1L);
        mockShowtimeDTO2.setMovieTitle("Test Movie 1");
        mockShowtimeDTO2.setMoviePosterUrl("http://poster1.url");
        mockShowtimeDTO2.setCinemaId(1L);
        mockShowtimeDTO2.setCinemaName("Test Cinema");
        mockShowtimeDTO2.setShowDateTime(LocalDateTime.now().plusDays(1).withHour(21).withMinute(0));
        mockShowtimeDTO2.setType(ShowtimeType.SPANISH_3D);
        mockShowtimeDTO2.setPrice(new BigDecimal("8000"));
        mockShowtimeDTO2.setAvailableSeats(90);
        mockShowtimeDTO2.setTotalSeats(120);
    }

    @Test
    @DisplayName("getCurrentWeekShowtimes - Retorna funciones de la semana actual")
    void getCurrentWeekShowtimes_ReturnsCurrentWeekShowtimes() {
        when(showtimeService.getCurrentWeekShowtimes()).thenReturn(Arrays.asList(mockShowtimeDTO1, mockShowtimeDTO2));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getCurrentWeekShowtimes();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(showtimeService, times(1)).getCurrentWeekShowtimes();
    }

    @Test
    @DisplayName("getCurrentWeekShowtimes - Retorna lista vacía cuando no hay funciones")
    void getCurrentWeekShowtimes_EmptyList_WhenNoShowtimes() {
        when(showtimeService.getCurrentWeekShowtimes()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getCurrentWeekShowtimes();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(showtimeService, times(1)).getCurrentWeekShowtimes();
    }

    @Test
    @DisplayName("getShowtimeById - Retorna función por ID")
    void getShowtimeById_ReturnsShowtime() {
        when(showtimeService.getShowtimeById(1L)).thenReturn(mockShowtimeDTO1);

        ResponseEntity<ShowtimeDTO> response = showtimeController.getShowtimeById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Movie 1", response.getBody().getMovieTitle());
        verify(showtimeService, times(1)).getShowtimeById(1L);
    }

    @Test
    @DisplayName("getShowtimesByMovie - Retorna funciones por película")
    void getShowtimesByMovie_ReturnsShowtimesForMovie() {
        when(showtimeService.getShowtimesByMovie(1L)).thenReturn(Arrays.asList(mockShowtimeDTO1, mockShowtimeDTO2));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByMovie(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(showtimeService, times(1)).getShowtimesByMovie(1L);
    }

    @Test
    @DisplayName("getShowtimesByMovie - Retorna lista vacía cuando no hay funciones para la película")
    void getShowtimesByMovie_EmptyList_WhenNoShowtimes() {
        when(showtimeService.getShowtimesByMovie(999L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByMovie(999L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(showtimeService, times(1)).getShowtimesByMovie(999L);
    }

    @Test
    @DisplayName("getShowtimesByCinema - Retorna funciones por cine")
    void getShowtimesByCinema_ReturnsShowtimesForCinema() {
        when(showtimeService.getShowtimesByCinema(1L)).thenReturn(Arrays.asList(mockShowtimeDTO1, mockShowtimeDTO2));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByCinema(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(showtimeService, times(1)).getShowtimesByCinema(1L);
    }

    @Test
    @DisplayName("getShowtimesByCinema - Retorna lista vacía cuando no hay funciones para el cine")
    void getShowtimesByCinema_EmptyList_WhenNoShowtimes() {
        when(showtimeService.getShowtimesByCinema(999L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByCinema(999L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(showtimeService, times(1)).getShowtimesByCinema(999L);
    }

    @Test
    @DisplayName("getShowtimesByCinemaAndMovie - Retorna funciones por cine y película")
    void getShowtimesByCinemaAndMovie_ReturnsShowtimes() {
        when(showtimeService.getShowtimesByCinemaAndMovie(1L, 1L))
                .thenReturn(Arrays.asList(mockShowtimeDTO1, mockShowtimeDTO2));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByCinemaAndMovie(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(showtimeService, times(1)).getShowtimesByCinemaAndMovie(1L, 1L);
    }

    @Test
    @DisplayName("getShowtimesByCinemaAndMovie - Retorna lista vacía cuando no hay coincidencias")
    void getShowtimesByCinemaAndMovie_EmptyList_WhenNoMatches() {
        when(showtimeService.getShowtimesByCinemaAndMovie(999L, 999L))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByCinemaAndMovie(999L, 999L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(showtimeService, times(1)).getShowtimesByCinemaAndMovie(999L, 999L);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Retorna funciones filtradas por película, cine y fecha")
    void getShowtimesByFilters_ReturnsFilteredShowtimes() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        when(showtimeService.getShowtimesByFilters(1L, 1L, targetDate))
                .thenReturn(Arrays.asList(mockShowtimeDTO1, mockShowtimeDTO2));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(showtimeService, times(1)).getShowtimesByFilters(1L, 1L, targetDate);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Retorna lista vacía cuando no hay funciones para los filtros")
    void getShowtimesByFilters_EmptyList_WhenNoMatches() {
        LocalDate targetDate = LocalDate.now().plusDays(10);
        when(showtimeService.getShowtimesByFilters(999L, 999L, targetDate))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByFilters(999L, 999L, targetDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(showtimeService, times(1)).getShowtimesByFilters(999L, 999L, targetDate);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Filtra correctamente por fecha específica")
    void getShowtimesByFilters_FiltersCorrectlyByDate() {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        when(showtimeService.getShowtimesByFilters(1L, 1L, targetDate))
                .thenReturn(Arrays.asList(mockShowtimeDTO1));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(mockShowtimeDTO1.getId(), response.getBody().get(0).getId());
        verify(showtimeService, times(1)).getShowtimesByFilters(1L, 1L, targetDate);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Maneja múltiples funciones en el mismo día")
    void getShowtimesByFilters_HandlesMultipleShowtimesOnSameDay() {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        ShowtimeDTO showtime1 = new ShowtimeDTO();
        showtime1.setId(1L);
        showtime1.setShowDateTime(LocalDateTime.of(targetDate, java.time.LocalTime.of(14, 0)));

        ShowtimeDTO showtime2 = new ShowtimeDTO();
        showtime2.setId(2L);
        showtime2.setShowDateTime(LocalDateTime.of(targetDate, java.time.LocalTime.of(17, 30)));

        ShowtimeDTO showtime3 = new ShowtimeDTO();
        showtime3.setId(3L);
        showtime3.setShowDateTime(LocalDateTime.of(targetDate, java.time.LocalTime.of(21, 0)));

        when(showtimeService.getShowtimesByFilters(1L, 1L, targetDate))
                .thenReturn(Arrays.asList(showtime1, showtime2, showtime3));

        ResponseEntity<List<ShowtimeDTO>> response = showtimeController.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(showtimeService, times(1)).getShowtimesByFilters(1L, 1L, targetDate);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Verifica que se llama con los parámetros correctos")
    void getShowtimesByFilters_CallsServiceWithCorrectParameters() {
        Long movieId = 5L;
        Long cinemaId = 3L;
        LocalDate date = LocalDate.of(2026, 3, 15);

        when(showtimeService.getShowtimesByFilters(movieId, cinemaId, date))
                .thenReturn(Collections.emptyList());

        showtimeController.getShowtimesByFilters(movieId, cinemaId, date);

        verify(showtimeService, times(1)).getShowtimesByFilters(movieId, cinemaId, date);
        verify(showtimeService, times(1)).getShowtimesByFilters(eq(5L), eq(3L), eq(date));
    }
}