package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.ShowtimeDTO;
import com.cinebook.backend.entities.*;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.repositories.CinemaRepository;
import com.cinebook.backend.repositories.MovieRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.repositories.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowtimeServiceImpl Tests")
class ShowtimeServiceImplTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;


    @InjectMocks
    private ShowtimeServiceImpl showtimeService;

    private Movie mockMovie;
    private Cinema mockCinema;
    private WeeklySchedule mockWeeklySchedule;
    private Showtime mockShowtime1;
    private Showtime mockShowtime2;

    @BeforeEach
    void setUp() {
        mockMovie = new Movie();
        mockMovie.setId(1L);
        mockMovie.setTitle("Test Movie");
        mockMovie.setPosterUrl("http://poster.url");
        mockMovie.setIsActive(true);

        mockCinema = new Cinema();
        mockCinema.setId(1L);
        mockCinema.setName("Test Cinema");
        mockCinema.setIsActive(true);

        mockWeeklySchedule = new WeeklySchedule();
        mockWeeklySchedule.setWeekId(1L);
        mockWeeklySchedule.setWeekStartDate(LocalDate.now());
        mockWeeklySchedule.setWeekEndDate(LocalDate.now().plusDays(6));
        mockWeeklySchedule.setIsActive(true);

        mockShowtime1 = createShowtime(1L, mockMovie, mockCinema,
            LocalDateTime.now().plusDays(1).withHour(17).withMinute(30),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        mockShowtime2 = createShowtime(2L, mockMovie, mockCinema,
            LocalDateTime.now().plusDays(1).withHour(21).withMinute(0),
            ShowtimeType.SPANISH_3D, new BigDecimal("8000"), 1L);
    }

    private Showtime createShowtime(Long id, Movie movie, Cinema cinema, LocalDateTime dateTime,
                                     ShowtimeType type, BigDecimal price, Long weekId) {
        Showtime showtime = new Showtime();
        showtime.setId(id);
        showtime.setMovie(movie);
        showtime.setCinema(cinema);
        showtime.setShowDateTime(dateTime);
        showtime.setType(type);
        showtime.setPrice(price);
        showtime.setWeekId(weekId);
        showtime.setAvailableSeats(100);
        showtime.setTotalSeats(120);
        return showtime;
    }

    @Test
    @DisplayName("getCurrentWeekShowtimes - Retorna funciones de la semana actual")
    void getCurrentWeekShowtimes_ReturnsCurrentWeekShowtimes() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(showtimeRepository.findByWeekId(1L)).thenReturn(Arrays.asList(mockShowtime1, mockShowtime2));

        List<ShowtimeDTO> result = showtimeService.getCurrentWeekShowtimes();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(weeklyScheduleRepository, times(1)).findAll();
        verify(showtimeRepository, times(1)).findByWeekId(1L);
    }

    @Test
    @DisplayName("getCurrentWeekShowtimes - Filtra funciones pasadas")
    void getCurrentWeekShowtimes_FiltersPastShowtimes() {
        Showtime pastShowtime = createShowtime(3L, mockMovie, mockCinema,
            LocalDateTime.now().minusHours(1),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(showtimeRepository.findByWeekId(1L)).thenReturn(Arrays.asList(mockShowtime1, pastShowtime));

        List<ShowtimeDTO> result = showtimeService.getCurrentWeekShowtimes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getShowDateTime().isAfter(LocalDateTime.now()));

        verify(weeklyScheduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getCurrentWeekShowtimes - Filtra películas inactivas")
    void getCurrentWeekShowtimes_FiltersInactiveMovies() {
        Movie inactiveMovie = new Movie();
        inactiveMovie.setId(2L);
        inactiveMovie.setTitle("Inactive Movie");
        inactiveMovie.setIsActive(false);

        Showtime showtimeWithInactiveMovie = createShowtime(3L, inactiveMovie, mockCinema,
            LocalDateTime.now().plusDays(1),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(showtimeRepository.findByWeekId(1L)).thenReturn(Arrays.asList(mockShowtime1, showtimeWithInactiveMovie));

        List<ShowtimeDTO> result = showtimeService.getCurrentWeekShowtimes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getMovieTitle());

        verify(weeklyScheduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getShowtimesByMovie - Retorna funciones de una película")
    void getShowtimesByMovie_ReturnsShowtimesForMovie() {
        when(showtimeRepository.findByMovieId(1L)).thenReturn(Arrays.asList(mockShowtime1, mockShowtime2));

        List<ShowtimeDTO> result = showtimeService.getShowtimesByMovie(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(showtimeRepository, times(1)).findByMovieId(1L);
    }

    @Test
    @DisplayName("getShowtimesByMovie - Lista vacía cuando no hay funciones")
    void getShowtimesByMovie_EmptyList_WhenNoShowtimes() {
        when(showtimeRepository.findByMovieId(999L)).thenReturn(Collections.emptyList());

        List<ShowtimeDTO> result = showtimeService.getShowtimesByMovie(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(showtimeRepository, times(1)).findByMovieId(999L);
    }

    @Test
    @DisplayName("getShowtimesByCinema - Retorna funciones de un cine")
    void getShowtimesByCinema_ReturnsShowtimesForCinema() {
        when(showtimeRepository.findByCinemaId(1L)).thenReturn(Arrays.asList(mockShowtime1, mockShowtime2));

        List<ShowtimeDTO> result = showtimeService.getShowtimesByCinema(1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(showtimeRepository, times(1)).findByCinemaId(1L);
    }

    @Test
    @DisplayName("getShowtimesByCinemaAndMovie - Retorna funciones filtradas y ordenadas")
    void getShowtimesByCinemaAndMovie_ReturnsFilteredAndSortedShowtimes() {
        when(showtimeRepository.findByCinemaIdAndMovieId(1L, 1L))
            .thenReturn(Arrays.asList(mockShowtime2, mockShowtime1));

        List<ShowtimeDTO> result = showtimeService.getShowtimesByCinemaAndMovie(1L, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getShowDateTime().isBefore(result.get(1).getShowDateTime()));

        verify(showtimeRepository, times(1)).findByCinemaIdAndMovieId(1L, 1L);
    }

    @Test
    @DisplayName("getShowtimeById - Retorna función cuando existe")
    void getShowtimeById_Found_ReturnsDTO() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(mockShowtime1));

        ShowtimeDTO result = showtimeService.getShowtimeById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Movie", result.getMovieTitle());

        verify(showtimeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getShowtimeById - Lanza excepción cuando no existe")
    void getShowtimeById_NotFound_ThrowsException() {
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            showtimeService.getShowtimeById(999L);
        });

        assertEquals("Función no encontrada con ID: 999", exception.getMessage());

        verify(showtimeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("generateShowtimesForCurrentWeek - Genera funciones correctamente")
    void generateShowtimesForCurrentWeek_GeneratesShowtimes() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(movieRepository.findByIsActiveTrueOrderByIdAsc()).thenReturn(Arrays.asList(mockMovie));
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockCinema));
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());
        when(showtimeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        showtimeService.generateShowtimesForCurrentWeek();

        verify(weeklyScheduleRepository, times(1)).findAll();
        verify(movieRepository, times(1)).findByIsActiveTrueOrderByIdAsc();
        verify(cinemaRepository, times(1)).findByIsActiveTrue();
        verify(showtimeRepository, atLeast(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("generateShowtimesForCurrentWeek - No genera cuando no hay películas")
    void generateShowtimesForCurrentWeek_DoesNotGenerate_WhenNoMovies() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(movieRepository.findByIsActiveTrueOrderByIdAsc()).thenReturn(Collections.emptyList());

        showtimeService.generateShowtimesForCurrentWeek();

        verify(movieRepository, times(1)).findByIsActiveTrueOrderByIdAsc();
        verify(showtimeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("generateShowtimesForCurrentWeek - No genera cuando no hay cines")
    void generateShowtimesForCurrentWeek_DoesNotGenerate_WhenNoCinemas() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(movieRepository.findByIsActiveTrueOrderByIdAsc()).thenReturn(Arrays.asList(mockMovie));
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        showtimeService.generateShowtimesForCurrentWeek();

        verify(movieRepository, times(1)).findByIsActiveTrueOrderByIdAsc();
        verify(cinemaRepository, times(1)).findByIsActiveTrue();
        verify(showtimeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("generateShowtimesForDate - Genera funciones para fecha específica")
    void generateShowtimesForDate_GeneratesShowtimesForSpecificDate() {
        LocalDate targetDate = LocalDate.now().plusDays(7);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockWeeklySchedule));
        when(movieRepository.findByIsActiveTrueOrderByIdAsc()).thenReturn(Arrays.asList(mockMovie));
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockCinema));
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());
        when(showtimeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        showtimeService.generateShowtimesForDate(targetDate);

        verify(weeklyScheduleRepository, times(1)).findAll();
        verify(movieRepository, times(1)).findByIsActiveTrueOrderByIdAsc();
        verify(cinemaRepository, times(1)).findByIsActiveTrue();
        verify(showtimeRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("convertToDTO - Convierte todos los campos correctamente")
    void convertToDTO_ConvertsAllFieldsCorrectly() {
        ShowtimeDTO result = showtimeService.convertToDTO(mockShowtime1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getMovieId());
        assertEquals("Test Movie", result.getMovieTitle());
        assertEquals("http://poster.url", result.getMoviePosterUrl());
        assertEquals(1L, result.getCinemaId());
        assertEquals("Test Cinema", result.getCinemaName());
        assertEquals(ShowtimeType.SPANISH_2D, result.getType());
        assertEquals(new BigDecimal("5000"), result.getPrice());
        assertEquals(100, result.getAvailableSeats());
        assertEquals(120, result.getTotalSeats());
    }

    @Test
    @DisplayName("getShowtimesByFilters - Filtra correctamente por película, cine y fecha")
    void getShowtimesByFilters_FiltersCorrectly() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        Showtime showtimeOnDate = createShowtime(3L, mockMovie, mockCinema,
            LocalDateTime.of(targetDate, LocalTime.of(19, 0)),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        when(showtimeRepository.findByCinemaIdAndMovieId(1L, 1L))
            .thenReturn(Arrays.asList(showtimeOnDate));

        List<ShowtimeDTO> result = showtimeService.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(targetDate, result.get(0).getShowDateTime().toLocalDate());

        verify(showtimeRepository, times(1)).findByCinemaIdAndMovieId(1L, 1L);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Filtra funciones fuera del rango de fecha")
    void getShowtimesByFilters_FiltersShowtimesOutsideDateRange() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        LocalDate differentDate = LocalDate.now().plusDays(2);

        Showtime showtimeOnTargetDate = createShowtime(3L, mockMovie, mockCinema,
            LocalDateTime.of(targetDate, LocalTime.of(19, 0)),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        Showtime showtimeOnDifferentDate = createShowtime(4L, mockMovie, mockCinema,
            LocalDateTime.of(differentDate, LocalTime.of(19, 0)),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        when(showtimeRepository.findByCinemaIdAndMovieId(1L, 1L))
            .thenReturn(Arrays.asList(showtimeOnTargetDate, showtimeOnDifferentDate));

        List<ShowtimeDTO> result = showtimeService.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(targetDate, result.get(0).getShowDateTime().toLocalDate());
    }

    @Test
    @DisplayName("getShowtimesByFilters - Retorna lista vacía cuando no hay coincidencias")
    void getShowtimesByFilters_EmptyList_WhenNoMatches() {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        when(showtimeRepository.findByCinemaIdAndMovieId(1L, 1L))
            .thenReturn(Collections.emptyList());

        List<ShowtimeDTO> result = showtimeService.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(showtimeRepository, times(1)).findByCinemaIdAndMovieId(1L, 1L);
    }

    @Test
    @DisplayName("getShowtimesByFilters - Ordena resultados por fecha y hora")
    void getShowtimesByFilters_SortsResultsByDateTime() {
        LocalDate targetDate = LocalDate.now().plusDays(1);

        Showtime showtime1 = createShowtime(3L, mockMovie, mockCinema,
            LocalDateTime.of(targetDate, LocalTime.of(21, 0)),
            ShowtimeType.SPANISH_3D, new BigDecimal("8000"), 1L);

        Showtime showtime2 = createShowtime(4L, mockMovie, mockCinema,
            LocalDateTime.of(targetDate, LocalTime.of(17, 30)),
            ShowtimeType.SPANISH_2D, new BigDecimal("5000"), 1L);

        when(showtimeRepository.findByCinemaIdAndMovieId(1L, 1L))
            .thenReturn(Arrays.asList(showtime1, showtime2));

        List<ShowtimeDTO> result = showtimeService.getShowtimesByFilters(1L, 1L, targetDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getShowDateTime().isBefore(result.get(1).getShowDateTime()));
    }
}

