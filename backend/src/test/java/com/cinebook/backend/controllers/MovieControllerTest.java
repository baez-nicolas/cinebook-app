package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.MovieDTO;
import com.cinebook.backend.services.interfaces.IMovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieController Tests")
class MovieControllerTest {

    @Mock
    private IMovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private MovieDTO mockActiveMovie1;
    private MovieDTO mockActiveMovie2;
    private MovieDTO mockInactiveMovie;

    @BeforeEach
    void setUp() {
        mockActiveMovie1 = new MovieDTO();
        mockActiveMovie1.setId(1L);
        mockActiveMovie1.setTitle("Test Movie 1");
        mockActiveMovie1.setDescription("Description 1");
        mockActiveMovie1.setDuration(120);
        mockActiveMovie1.setGenre("Action");
        mockActiveMovie1.setRating("PG_13");
        mockActiveMovie1.setPosterUrl("http://poster1.url");
        mockActiveMovie1.setTrailerUrl("http://trailer1.url");
        mockActiveMovie1.setReleaseDate(LocalDate.of(2026, 3, 1));
        mockActiveMovie1.setIsActive(true);

        mockActiveMovie2 = new MovieDTO();
        mockActiveMovie2.setId(2L);
        mockActiveMovie2.setTitle("Test Movie 2");
        mockActiveMovie2.setDescription("Description 2");
        mockActiveMovie2.setDuration(150);
        mockActiveMovie2.setGenre("Drama");
        mockActiveMovie2.setRating("R");
        mockActiveMovie2.setPosterUrl("http://poster2.url");
        mockActiveMovie2.setTrailerUrl("http://trailer2.url");
        mockActiveMovie2.setReleaseDate(LocalDate.of(2026, 4, 1));
        mockActiveMovie2.setIsActive(true);

        mockInactiveMovie = new MovieDTO();
        mockInactiveMovie.setId(3L);
        mockInactiveMovie.setTitle("Inactive Movie");
        mockInactiveMovie.setDescription("Inactive Description");
        mockInactiveMovie.setDuration(90);
        mockInactiveMovie.setGenre("Comedy");
        mockInactiveMovie.setRating("PG");
        mockInactiveMovie.setPosterUrl("http://poster3.url");
        mockInactiveMovie.setTrailerUrl("http://trailer3.url");
        mockInactiveMovie.setReleaseDate(LocalDate.of(2025, 1, 1));
        mockInactiveMovie.setIsActive(false);
    }

    @Test
    @DisplayName("getAllActiveMovies - Retorna todas las películas activas")
    void getAllActiveMovies_ReturnsAllActiveMovies() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie1, mockActiveMovie2));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(MovieDTO::getIsActive));

        verify(movieService, times(1)).getAllActiveMovies();
    }

    @Test
    @DisplayName("getAllActiveMovies - Retorna lista vacía cuando no hay películas activas")
    void getAllActiveMovies_ReturnsEmptyList_WhenNoActiveMovies() {
        when(movieService.getAllActiveMovies()).thenReturn(Collections.emptyList());

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(movieService, times(1)).getAllActiveMovies();
    }

    @Test
    @DisplayName("getAllActiveMovies - Retorna información completa de las películas")
    void getAllActiveMovies_ReturnsCompleteInfo() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie1));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        MovieDTO movie = response.getBody().get(0);
        assertAll("Movie info",
            () -> assertEquals(1L, movie.getId()),
            () -> assertEquals("Test Movie 1", movie.getTitle()),
            () -> assertEquals("Description 1", movie.getDescription()),
            () -> assertEquals(120, movie.getDuration()),
            () -> assertEquals("Action", movie.getGenre()),
            () -> assertEquals("PG_13", movie.getRating()),
            () -> assertNotNull(movie.getPosterUrl()),
            () -> assertNotNull(movie.getTrailerUrl()),
            () -> assertNotNull(movie.getReleaseDate()),
            () -> assertTrue(movie.getIsActive())
        );
    }

    @Test
    @DisplayName("getAllActiveMovies - Solo retorna películas activas")
    void getAllActiveMovies_OnlyReturnsActive() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie1, mockActiveMovie2));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        assertTrue(response.getBody().stream().allMatch(m -> m.getIsActive()));
        verify(movieService, times(1)).getAllActiveMovies();
        verify(movieService, never()).getAllMovies();
    }

    @Test
    @DisplayName("getMovieById - Retorna película por ID")
    void getMovieById_ReturnsMovie() {
        when(movieService.getMovieById(1L)).thenReturn(mockActiveMovie1);

        ResponseEntity<MovieDTO> response = movieController.getMovieById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Movie 1", response.getBody().getTitle());

        verify(movieService, times(1)).getMovieById(1L);
    }

    @Test
    @DisplayName("getMovieById - Retorna información completa de la película")
    void getMovieById_ReturnsCompleteInfo() {
        when(movieService.getMovieById(1L)).thenReturn(mockActiveMovie1);

        ResponseEntity<MovieDTO> response = movieController.getMovieById(1L);

        MovieDTO movie = response.getBody();
        assertNotNull(movie);
        assertAll("Movie details",
            () -> assertNotNull(movie.getId()),
            () -> assertNotNull(movie.getTitle()),
            () -> assertNotNull(movie.getDescription()),
            () -> assertNotNull(movie.getDuration()),
            () -> assertNotNull(movie.getGenre()),
            () -> assertNotNull(movie.getRating()),
            () -> assertNotNull(movie.getPosterUrl()),
            () -> assertNotNull(movie.getTrailerUrl()),
            () -> assertNotNull(movie.getReleaseDate())
        );
    }

    @Test
    @DisplayName("getMovieById - Lanza excepción cuando ID no existe")
    void getMovieById_ThrowsException_WhenIdNotFound() {
        when(movieService.getMovieById(999L))
                .thenThrow(new RuntimeException("Película no encontrada con ID: 999"));

        assertThrows(RuntimeException.class, () -> {
            movieController.getMovieById(999L);
        });

        verify(movieService, times(1)).getMovieById(999L);
    }

    @Test
    @DisplayName("getMovieById - Puede retornar película inactiva")
    void getMovieById_CanReturnInactiveMovie() {
        when(movieService.getMovieById(3L)).thenReturn(mockInactiveMovie);

        ResponseEntity<MovieDTO> response = movieController.getMovieById(3L);

        assertEquals(3L, response.getBody().getId());
        assertFalse(response.getBody().getIsActive());
    }

    @Test
    @DisplayName("getAllMovies - Retorna todas las películas (activas e inactivas)")
    void getAllMovies_ReturnsAllMovies() {
        when(movieService.getAllMovies())
                .thenReturn(Arrays.asList(mockActiveMovie1, mockActiveMovie2, mockInactiveMovie));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllMovies();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());

        verify(movieService, times(1)).getAllMovies();
    }

    @Test
    @DisplayName("getAllMovies - Incluye películas activas e inactivas")
    void getAllMovies_IncludesActiveAndInactive() {
        when(movieService.getAllMovies())
                .thenReturn(Arrays.asList(mockActiveMovie1, mockInactiveMovie));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllMovies();

        assertTrue(response.getBody().stream().anyMatch(MovieDTO::getIsActive));
        assertTrue(response.getBody().stream().anyMatch(m -> !m.getIsActive()));
    }

    @Test
    @DisplayName("getAllMovies - Retorna lista vacía cuando no hay películas")
    void getAllMovies_ReturnsEmptyList_WhenNoMovies() {
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());

        ResponseEntity<List<MovieDTO>> response = movieController.getAllMovies();

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getMovieById - Retorna diferentes películas según ID")
    void getMovieById_ReturnsDifferentMovies() {
        when(movieService.getMovieById(1L)).thenReturn(mockActiveMovie1);
        when(movieService.getMovieById(2L)).thenReturn(mockActiveMovie2);

        ResponseEntity<MovieDTO> response1 = movieController.getMovieById(1L);
        ResponseEntity<MovieDTO> response2 = movieController.getMovieById(2L);

        assertNotEquals(response1.getBody().getId(), response2.getBody().getId());
        assertNotEquals(response1.getBody().getTitle(), response2.getBody().getTitle());
    }

    @Test
    @DisplayName("getAllActiveMovies - Retorna películas de diferentes géneros")
    void getAllActiveMovies_ReturnsDifferentGenres() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie1, mockActiveMovie2));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        List<MovieDTO> movies = response.getBody();
        assertEquals("Action", movies.get(0).getGenre());
        assertEquals("Drama", movies.get(1).getGenre());
    }

    @Test
    @DisplayName("getAllActiveMovies - Retorna películas con diferentes ratings")
    void getAllActiveMovies_ReturnsDifferentRatings() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie1, mockActiveMovie2));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        List<MovieDTO> movies = response.getBody();
        assertEquals("PG_13", movies.get(0).getRating());
        assertEquals("R", movies.get(1).getRating());
    }

    @Test
    @DisplayName("getMovieById - Verifica llamada única al servicio")
    void getMovieById_CallsServiceOnce() {
        when(movieService.getMovieById(1L)).thenReturn(mockActiveMovie1);

        movieController.getMovieById(1L);

        verify(movieService, times(1)).getMovieById(1L);
        verifyNoMoreInteractions(movieService);
    }

    @Test
    @DisplayName("getAllActiveMovies - Verifica llamada única al servicio")
    void getAllActiveMovies_CallsServiceOnce() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie1));

        movieController.getAllActiveMovies();

        verify(movieService, times(1)).getAllActiveMovies();
        verifyNoMoreInteractions(movieService);
    }

    @Test
    @DisplayName("getAllMovies - Verifica llamada única al servicio")
    void getAllMovies_CallsServiceOnce() {
        when(movieService.getAllMovies()).thenReturn(Arrays.asList(mockActiveMovie1, mockInactiveMovie));

        movieController.getAllMovies();

        verify(movieService, times(1)).getAllMovies();
        verifyNoMoreInteractions(movieService);
    }

    @Test
    @DisplayName("getAllActiveMovies - Mantiene orden de la base de datos")
    void getAllActiveMovies_MaintainsDatabaseOrder() {
        when(movieService.getAllActiveMovies()).thenReturn(Arrays.asList(mockActiveMovie2, mockActiveMovie1));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllActiveMovies();

        List<MovieDTO> movies = response.getBody();
        assertEquals("Test Movie 2", movies.get(0).getTitle());
        assertEquals("Test Movie 1", movies.get(1).getTitle());
    }

    @Test
    @DisplayName("getMovieById - Retorna duración correcta")
    void getMovieById_ReturnsCorrectDuration() {
        when(movieService.getMovieById(1L)).thenReturn(mockActiveMovie1);

        ResponseEntity<MovieDTO> response = movieController.getMovieById(1L);

        assertEquals(120, response.getBody().getDuration());
    }

    @Test
    @DisplayName("getAllMovies - Cuenta correctamente películas activas e inactivas")
    void getAllMovies_CountsActiveAndInactiveCorrectly() {
        when(movieService.getAllMovies())
                .thenReturn(Arrays.asList(mockActiveMovie1, mockActiveMovie2, mockInactiveMovie));

        ResponseEntity<List<MovieDTO>> response = movieController.getAllMovies();

        long activeCount = response.getBody().stream().filter(MovieDTO::getIsActive).count();
        long inactiveCount = response.getBody().stream().filter(m -> !m.getIsActive()).count();

        assertEquals(2, activeCount);
        assertEquals(1, inactiveCount);
    }
}

