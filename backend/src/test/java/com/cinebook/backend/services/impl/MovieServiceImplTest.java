package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.MovieDTO;
import com.cinebook.backend.entities.Movie;
import com.cinebook.backend.entities.enums.MovieRating;
import com.cinebook.backend.repositories.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieServiceImpl Tests")
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie mockMovie1;
    private Movie mockMovie2;
    private Movie mockMovie3;

    @BeforeEach
    void setUp() {
        mockMovie1 = new Movie();
        mockMovie1.setId(1L);
        mockMovie1.setTitle("Avengers: Endgame");
        mockMovie1.setDescription("Los vengadores se reúnen una vez más");
        mockMovie1.setDuration(181);
        mockMovie1.setGenre("Acción");
        mockMovie1.setRating(MovieRating.PG_13);
        mockMovie1.setPosterUrl("http://poster1.url");
        mockMovie1.setTrailerUrl("http://trailer1.url");
        mockMovie1.setReleaseDate(LocalDate.of(2019, 4, 26));
        mockMovie1.setIsActive(true);

        mockMovie2 = new Movie();
        mockMovie2.setId(2L);
        mockMovie2.setTitle("The Dark Knight");
        mockMovie2.setDescription("Batman enfrenta al Joker");
        mockMovie2.setDuration(152);
        mockMovie2.setGenre("Drama");
        mockMovie2.setRating(MovieRating.PG_13);
        mockMovie2.setPosterUrl("http://poster2.url");
        mockMovie2.setTrailerUrl("http://trailer2.url");
        mockMovie2.setReleaseDate(LocalDate.of(2008, 7, 18));
        mockMovie2.setIsActive(true);

        mockMovie3 = new Movie();
        mockMovie3.setId(3L);
        mockMovie3.setTitle("Old Movie");
        mockMovie3.setDescription("Una película antigua");
        mockMovie3.setDuration(120);
        mockMovie3.setGenre("Clásico");
        mockMovie3.setRating(MovieRating.PG);
        mockMovie3.setPosterUrl("http://poster3.url");
        mockMovie3.setTrailerUrl("http://trailer3.url");
        mockMovie3.setReleaseDate(LocalDate.of(2000, 1, 1));
        mockMovie3.setIsActive(false);
    }

    @Test
    @DisplayName("getAllActiveMovies - Retorna solo películas activas")
    void getAllActiveMovies_ReturnsOnlyActiveMovies() {
        when(movieRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockMovie1, mockMovie2));

        List<MovieDTO> result = movieService.getAllActiveMovies();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Avengers: Endgame", result.get(0).getTitle());
        assertEquals("The Dark Knight", result.get(1).getTitle());
        assertTrue(result.get(0).getIsActive());
        assertTrue(result.get(1).getIsActive());

        verify(movieRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAllActiveMovies - Lista vacía cuando no hay películas activas")
    void getAllActiveMovies_EmptyList_WhenNoActiveMovies() {
        when(movieRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        List<MovieDTO> result = movieService.getAllActiveMovies();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(movieRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAllActiveMovies - Convierte correctamente a DTO")
    void getAllActiveMovies_ConvertsToDTO_Correctly() {
        when(movieRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockMovie1));

        List<MovieDTO> result = movieService.getAllActiveMovies();

        assertNotNull(result);
        assertEquals(1, result.size());
        MovieDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Avengers: Endgame", dto.getTitle());
        assertEquals("Los vengadores se reúnen una vez más", dto.getDescription());
        assertEquals(181, dto.getDuration());
        assertEquals("Acción", dto.getGenre());
        assertEquals("PG_13", dto.getRating());
        assertEquals("http://poster1.url", dto.getPosterUrl());
        assertEquals("http://trailer1.url", dto.getTrailerUrl());
        assertEquals(LocalDate.of(2019, 4, 26), dto.getReleaseDate());
        assertTrue(dto.getIsActive());

        verify(movieRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getMovieById - Retorna película cuando existe")
    void getMovieById_Found_ReturnsDTO() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie1));

        MovieDTO result = movieService.getMovieById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Avengers: Endgame", result.getTitle());
        assertEquals(181, result.getDuration());

        verify(movieRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getMovieById - Lanza excepción cuando no existe")
    void getMovieById_NotFound_ThrowsException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            movieService.getMovieById(999L);
        });

        assertEquals("Película no encontrada con ID: 999", exception.getMessage());

        verify(movieRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getMovieById - Retorna película inactiva si se busca por ID")
    void getMovieById_ReturnsInactiveMovie() {
        when(movieRepository.findById(3L)).thenReturn(Optional.of(mockMovie3));

        MovieDTO result = movieService.getMovieById(3L);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Old Movie", result.getTitle());
        assertFalse(result.getIsActive());

        verify(movieRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("getAllMovies - Retorna todas las películas (activas e inactivas)")
    void getAllMovies_ReturnsAllMovies() {
        when(movieRepository.findAll()).thenReturn(Arrays.asList(mockMovie1, mockMovie2, mockMovie3));

        List<MovieDTO> result = movieService.getAllMovies();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Avengers: Endgame", result.get(0).getTitle());
        assertEquals("The Dark Knight", result.get(1).getTitle());
        assertEquals("Old Movie", result.get(2).getTitle());

        verify(movieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllMovies - Lista vacía cuando no hay películas")
    void getAllMovies_EmptyList_WhenNoMovies() {
        when(movieRepository.findAll()).thenReturn(Collections.emptyList());

        List<MovieDTO> result = movieService.getAllMovies();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(movieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("convertToDTO - Convierte todos los campos correctamente")
    void convertToDTO_ConvertsAllFieldsCorrectly() {
        MovieDTO result = movieService.convertToDTO(mockMovie1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Avengers: Endgame", result.getTitle());
        assertEquals("Los vengadores se reúnen una vez más", result.getDescription());
        assertEquals(181, result.getDuration());
        assertEquals("Acción", result.getGenre());
        assertEquals("PG_13", result.getRating());
        assertEquals("http://poster1.url", result.getPosterUrl());
        assertEquals("http://trailer1.url", result.getTrailerUrl());
        assertEquals(LocalDate.of(2019, 4, 26), result.getReleaseDate());
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("convertToDTO - Maneja rating null correctamente")
    void convertToDTO_HandlesNullRating() {
        Movie movieWithoutRating = new Movie();
        movieWithoutRating.setId(4L);
        movieWithoutRating.setTitle("Movie Without Rating");
        movieWithoutRating.setDescription("Sin clasificación");
        movieWithoutRating.setDuration(90);
        movieWithoutRating.setGenre("Drama");
        movieWithoutRating.setRating(null);
        movieWithoutRating.setPosterUrl("http://poster.url");
        movieWithoutRating.setTrailerUrl("http://trailer.url");
        movieWithoutRating.setReleaseDate(LocalDate.now());
        movieWithoutRating.setIsActive(true);

        MovieDTO result = movieService.convertToDTO(movieWithoutRating);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("Movie Without Rating", result.getTitle());
        assertNull(result.getRating());
    }

    @Test
    @DisplayName("convertToDTO - Convierte diferentes ratings correctamente")
    void convertToDTO_ConvertsDifferentRatings() {
        Movie movieG = new Movie();
        movieG.setId(5L);
        movieG.setTitle("Kids Movie");
        movieG.setRating(MovieRating.G);
        movieG.setDuration(90);
        movieG.setGenre("Infantil");
        movieG.setPosterUrl("http://poster.url");
        movieG.setReleaseDate(LocalDate.now());
        movieG.setIsActive(true);

        MovieDTO resultG = movieService.convertToDTO(movieG);
        assertEquals("G", resultG.getRating());

        movieG.setRating(MovieRating.R);
        MovieDTO resultR = movieService.convertToDTO(movieG);
        assertEquals("R", resultR.getRating());

        movieG.setRating(MovieRating.PG);
        MovieDTO resultPG = movieService.convertToDTO(movieG);
        assertEquals("PG", resultPG.getRating());
    }

    @Test
    @DisplayName("getAllActiveMovies - Mantiene el orden de la base de datos")
    void getAllActiveMovies_MaintainsDatabaseOrder() {
        when(movieRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockMovie2, mockMovie1));

        List<MovieDTO> result = movieService.getAllActiveMovies();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("The Dark Knight", result.get(0).getTitle());
        assertEquals("Avengers: Endgame", result.get(1).getTitle());

        verify(movieRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAllMovies - Incluye películas activas e inactivas")
    void getAllMovies_IncludesActiveAndInactiveMovies() {
        when(movieRepository.findAll()).thenReturn(Arrays.asList(mockMovie1, mockMovie3));

        List<MovieDTO> result = movieService.getAllMovies();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getTitle().equals("Avengers: Endgame") && dto.getIsActive()));
        assertTrue(result.stream().anyMatch(dto -> dto.getTitle().equals("Old Movie") && !dto.getIsActive()));

        verify(movieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getMovieById - Llama al repositorio con el ID correcto")
    void getMovieById_CallsRepositoryWithCorrectId() {
        when(movieRepository.findById(2L)).thenReturn(Optional.of(mockMovie2));

        movieService.getMovieById(2L);

        verify(movieRepository, times(1)).findById(2L);
        verify(movieRepository, never()).findById(1L);
        verify(movieRepository, never()).findAll();
    }

    @Test
    @DisplayName("convertToDTO - No modifica la entidad original")
    void convertToDTO_DoesNotModifyOriginalEntity() {
        Movie original = new Movie();
        original.setId(6L);
        original.setTitle("Original Title");
        original.setDescription("Original Description");
        original.setDuration(100);
        original.setGenre("Original Genre");
        original.setRating(MovieRating.PG_13);
        original.setPosterUrl("http://original.url");
        original.setTrailerUrl("http://trailer.url");
        original.setReleaseDate(LocalDate.of(2020, 1, 1));
        original.setIsActive(true);

        MovieDTO dto = movieService.convertToDTO(original);

        assertEquals("Original Title", original.getTitle());
        assertEquals("Original Description", original.getDescription());
        assertEquals("Original Title", dto.getTitle());
        assertEquals("Original Description", dto.getDescription());
    }

    @Test
    @DisplayName("convertToDTO - Maneja valores nulos en campos opcionales")
    void convertToDTO_HandlesNullOptionalFields() {
        Movie movieWithNulls = new Movie();
        movieWithNulls.setId(7L);
        movieWithNulls.setTitle("Movie With Nulls");
        movieWithNulls.setDescription(null);
        movieWithNulls.setDuration(100);
        movieWithNulls.setGenre("Drama");
        movieWithNulls.setRating(null);
        movieWithNulls.setPosterUrl(null);
        movieWithNulls.setTrailerUrl(null);
        movieWithNulls.setReleaseDate(null);
        movieWithNulls.setIsActive(true);

        MovieDTO result = movieService.convertToDTO(movieWithNulls);

        assertNotNull(result);
        assertEquals(7L, result.getId());
        assertEquals("Movie With Nulls", result.getTitle());
        assertNull(result.getDescription());
        assertNull(result.getRating());
        assertNull(result.getPosterUrl());
        assertNull(result.getTrailerUrl());
        assertNull(result.getReleaseDate());
    }
}

