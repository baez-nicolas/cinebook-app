package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.CreateMovieRequest;
import com.cinebook.backend.dtos.ReassignShowtimesRequest;
import com.cinebook.backend.entities.Movie;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.enums.MovieRating;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.repositories.MovieRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminMovieController Tests")
class AdminMovieControllerTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AdminMovieController adminMovieController;

    private Movie mockMovie;
    private CreateMovieRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockMovie = new Movie();
        mockMovie.setId(1L);
        mockMovie.setTitle("Test Movie");
        mockMovie.setDescription("Test Description");
        mockMovie.setDuration(120);
        mockMovie.setGenre("Action");
        mockMovie.setRating(MovieRating.PG_13);
        mockMovie.setPosterUrl("http://poster.url");
        mockMovie.setTrailerUrl("http://trailer.url");
        mockMovie.setReleaseDate(LocalDate.of(2026, 3, 1));
        mockMovie.setIsActive(true);

        mockRequest = new CreateMovieRequest();
        mockRequest.setTitle("New Movie");
        mockRequest.setDescription("New Description");
        mockRequest.setDuration(150);
        mockRequest.setGenre("Drama");
        mockRequest.setRating("PG_13");
        mockRequest.setPosterUrl("http://newposter.url");
        mockRequest.setTrailerUrl("http://newtrailer.url");
        mockRequest.setReleaseDate(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("countActiveMovies - Retorna conteo correcto")
    void countActiveMovies_ReturnsCorrectCount() {
        when(movieRepository.countByIsActiveTrue()).thenReturn(5L);
        when(movieRepository.count()).thenReturn(8L);

        ResponseEntity<Map<String, Object>> response = adminMovieController.countActiveMovies();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();

        assertEquals(5L, body.get("activeMovies"));
        assertEquals(8L, body.get("totalMovies"));
        assertEquals(12, body.get("maxMovies"));
        assertEquals(true, body.get("canAddMore"));

        verify(movieRepository, times(1)).countByIsActiveTrue();
        verify(movieRepository, times(1)).count();
    }

    @Test
    @DisplayName("countActiveMovies - Indica cuando no se pueden agregar más películas")
    void countActiveMovies_IndicatesCannotAddMore() {
        when(movieRepository.countByIsActiveTrue()).thenReturn(12L);
        when(movieRepository.count()).thenReturn(15L);

        ResponseEntity<Map<String, Object>> response = adminMovieController.countActiveMovies();

        Map<String, Object> body = response.getBody();
        assertEquals(false, body.get("canAddMore"));
    }

    @Test
    @DisplayName("createMovie - Crea película exitosamente")
    void createMovie_CreatesSuccessfully() {
        when(movieRepository.countByIsActiveTrue()).thenReturn(5L);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = adminMovieController.createMovie(mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertTrue(body.containsKey("movie"));
        assertEquals("Película creada exitosamente", body.get("message"));

        verify(movieRepository, times(1)).countByIsActiveTrue();
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("createMovie - Rechaza cuando se alcanza el límite")
    void createMovie_RejectsWhenLimitReached() {
        when(movieRepository.countByIsActiveTrue()).thenReturn(12L);

        ResponseEntity<?> response = adminMovieController.createMovie(mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("Límite alcanzado", body.get("error"));

        verify(movieRepository, times(1)).countByIsActiveTrue();
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("createMovie - Maneja rating null correctamente")
    void createMovie_HandlesNullRating() {
        mockRequest.setRating(null);

        when(movieRepository.countByIsActiveTrue()).thenReturn(5L);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = adminMovieController.createMovie(mockRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    @DisplayName("updateMovie - Actualiza película exitosamente")
    void updateMovie_UpdatesSuccessfully() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(mockMovie);

        ResponseEntity<?> response = adminMovieController.updateMovie(1L, mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("Película actualizada exitosamente", body.get("message"));
        assertEquals("New Movie", mockMovie.getTitle());

        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, times(1)).save(mockMovie);
    }

    @Test
    @DisplayName("updateMovie - Lanza excepción cuando película no existe")
    void updateMovie_ThrowsException_WhenNotFound() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            adminMovieController.updateMovie(999L, mockRequest);
        });

        verify(movieRepository, times(1)).findById(999L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("deleteMovie - Elimina película y marca como inactiva")
    void deleteMovie_DeletesAndMarksInactive() {
        Showtime showtime = new Showtime();
        showtime.setId(1L);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(showtimeRepository.findByMovieId(1L)).thenReturn(Arrays.asList(showtime));
        when(bookingRepository.deleteByShowtimeIdIn(anyList())).thenReturn(5);
        when(movieRepository.save(any(Movie.class))).thenReturn(mockMovie);

        ResponseEntity<?> response = adminMovieController.deleteMovie(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("Película eliminada exitosamente", body.get("message"));
        assertEquals("Test Movie", body.get("movieTitle"));
        assertEquals(1, body.get("orphanShowtimes"));
        assertEquals(5, body.get("deletedBookings"));
        assertFalse(mockMovie.getIsActive());

        verify(movieRepository, times(1)).save(mockMovie);
    }

    @Test
    @DisplayName("deleteMovie - Rechaza si película ya está eliminada")
    void deleteMovie_Rejects_WhenAlreadyDeleted() {
        mockMovie.setIsActive(false);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));

        ResponseEntity<?> response = adminMovieController.deleteMovie(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("La película ya está eliminada", body.get("error"));

        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    @DisplayName("deleteMovie - Lanza excepción cuando película no existe")
    void deleteMovie_ThrowsException_WhenNotFound() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            adminMovieController.deleteMovie(999L);
        });

        verify(movieRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getOrphanShowtimesCount - Cuenta funciones huérfanas correctamente")
    void getOrphanShowtimesCount_CountsCorrectly() {
        Movie inactiveMovie = new Movie();
        inactiveMovie.setIsActive(false);

        Showtime orphanShowtime = new Showtime();
        orphanShowtime.setMovie(inactiveMovie);

        when(showtimeRepository.findAll()).thenReturn(Arrays.asList(orphanShowtime));

        ResponseEntity<Map<String, Object>> response = adminMovieController.getOrphanShowtimesCount();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();

        assertEquals(1L, body.get("orphanShowtimes"));
        assertEquals(true, body.get("available"));
    }

    @Test
    @DisplayName("getOrphanShowtimesCount - Retorna cero cuando no hay huérfanas")
    void getOrphanShowtimesCount_ReturnsZero_WhenNone() {
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> response = adminMovieController.getOrphanShowtimesCount();

        Map<String, Object> body = response.getBody();

        assertEquals(0L, body.get("orphanShowtimes"));
        assertEquals(false, body.get("available"));
    }

    @Test
    @DisplayName("reassignShowtimes - Reasigna funciones huérfanas exitosamente")
    void reassignShowtimes_ReassignsSuccessfully() {
        Movie inactiveMovie = new Movie();
        inactiveMovie.setIsActive(false);

        Showtime orphanShowtime = new Showtime();
        orphanShowtime.setId(1L);
        orphanShowtime.setMovie(inactiveMovie);
        orphanShowtime.setType(ShowtimeType.SPANISH_2D);

        Movie newMovie = new Movie();
        newMovie.setId(2L);
        newMovie.setTitle("New Target Movie");
        newMovie.setIsActive(true);

        ReassignShowtimesRequest request = new ReassignShowtimesRequest();
        request.setMovieId(2L);

        when(movieRepository.findById(2L)).thenReturn(Optional.of(newMovie));
        when(showtimeRepository.findAll()).thenReturn(Arrays.asList(orphanShowtime));
        when(showtimeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = adminMovieController.reassignShowtimes(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(1, body.get("reassignedCount"));
        assertEquals("New Target Movie", body.get("movieTitle"));
        assertEquals(newMovie, orphanShowtime.getMovie());

        verify(showtimeRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("reassignShowtimes - Rechaza si película no existe")
    void reassignShowtimes_Rejects_WhenMovieNotFound() {
        ReassignShowtimesRequest request = new ReassignShowtimesRequest();
        request.setMovieId(999L);

        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            adminMovieController.reassignShowtimes(request);
        });

        verify(movieRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("reassignShowtimes - Rechaza si película no está activa")
    void reassignShowtimes_Rejects_WhenMovieInactive() {
        Movie inactiveMovie = new Movie();
        inactiveMovie.setId(2L);
        inactiveMovie.setIsActive(false);

        ReassignShowtimesRequest request = new ReassignShowtimesRequest();
        request.setMovieId(2L);

        when(movieRepository.findById(2L)).thenReturn(Optional.of(inactiveMovie));

        ResponseEntity<?> response = adminMovieController.reassignShowtimes(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("La película no está activa", body.get("error"));
    }

    @Test
    @DisplayName("reassignShowtimes - Rechaza si no hay funciones huérfanas")
    void reassignShowtimes_Rejects_WhenNoOrphanShowtimes() {
        Movie activeMovie = new Movie();
        activeMovie.setId(2L);
        activeMovie.setIsActive(true);

        ReassignShowtimesRequest request = new ReassignShowtimesRequest();
        request.setMovieId(2L);

        when(movieRepository.findById(2L)).thenReturn(Optional.of(activeMovie));
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = adminMovieController.reassignShowtimes(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("No hay funciones huérfanas disponibles", body.get("error"));
    }

    @Test
    @DisplayName("reassignShowtimes - Actualiza precios según tipo de función")
    void reassignShowtimes_UpdatesPricesCorrectly() {
        Movie inactiveMovie = new Movie();
        inactiveMovie.setIsActive(false);

        Showtime showtime2D = new Showtime();
        showtime2D.setMovie(inactiveMovie);
        showtime2D.setType(ShowtimeType.SPANISH_2D);

        Showtime showtime3D = new Showtime();
        showtime3D.setMovie(inactiveMovie);
        showtime3D.setType(ShowtimeType.SPANISH_3D);

        Movie newMovie = new Movie();
        newMovie.setId(2L);
        newMovie.setTitle("Target Movie");
        newMovie.setIsActive(true);

        ReassignShowtimesRequest request = new ReassignShowtimesRequest();
        request.setMovieId(2L);

        when(movieRepository.findById(2L)).thenReturn(Optional.of(newMovie));
        when(showtimeRepository.findAll()).thenReturn(Arrays.asList(showtime2D, showtime3D));
        when(showtimeRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        adminMovieController.reassignShowtimes(request);

        assertEquals(new BigDecimal("5000"), showtime2D.getPrice());
        assertEquals(new BigDecimal("8000"), showtime3D.getPrice());
    }

    @Test
    @DisplayName("createMovie - Incluye conteo de funciones huérfanas en respuesta")
    void createMovie_IncludesOrphanShowtimesCount() {
        Movie inactiveMovie = new Movie();
        inactiveMovie.setIsActive(false);

        Showtime orphanShowtime = new Showtime();
        orphanShowtime.setMovie(inactiveMovie);

        when(movieRepository.countByIsActiveTrue()).thenReturn(5L);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(showtimeRepository.findAll()).thenReturn(Arrays.asList(orphanShowtime));

        ResponseEntity<?> response = adminMovieController.createMovie(mockRequest);

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1L, body.get("orphanShowtimesAvailable"));
    }

    @Test
    @DisplayName("deleteMovie - No elimina reservas si no hay funciones")
    void deleteMovie_DoesNotDeleteBookings_WhenNoShowtimes() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(showtimeRepository.findByMovieId(1L)).thenReturn(Collections.emptyList());
        when(movieRepository.save(any(Movie.class))).thenReturn(mockMovie);

        ResponseEntity<?> response = adminMovieController.deleteMovie(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(0, body.get("deletedBookings"));

        verify(bookingRepository, never()).deleteByShowtimeIdIn(anyList());
    }
}

