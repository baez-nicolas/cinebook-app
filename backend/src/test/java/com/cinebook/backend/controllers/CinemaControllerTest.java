package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.CinemaDTO;
import com.cinebook.backend.services.interfaces.ICinemaService;
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
@DisplayName("CinemaController Tests")
class CinemaControllerTest {

    @Mock
    private ICinemaService cinemaService;

    @InjectMocks
    private CinemaController cinemaController;

    private CinemaDTO mockCinema1;
    private CinemaDTO mockCinema2;

    @BeforeEach
    void setUp() {
        mockCinema1 = new CinemaDTO();
        mockCinema1.setId(1L);
        mockCinema1.setName("Cinema Test 1");
        mockCinema1.setAddress("Calle Test 123");
        mockCinema1.setCity("Buenos Aires");
        mockCinema1.setPhone("123456789");

        mockCinema2 = new CinemaDTO();
        mockCinema2.setId(2L);
        mockCinema2.setName("Cinema Test 2");
        mockCinema2.setAddress("Avenida Test 456");
        mockCinema2.setCity("Córdoba");
        mockCinema2.setPhone("987654321");
    }

    @Test
    @DisplayName("getAllActiveCinemas - Retorna todos los cines activos")
    void getAllActiveCinemas_ReturnsAllActiveCinemas() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Arrays.asList(mockCinema1, mockCinema2));

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(cinemaService, times(1)).getAllActiveCinemas();
    }

    @Test
    @DisplayName("getAllActiveCinemas - Retorna lista vacía cuando no hay cines")
    void getAllActiveCinemas_ReturnsEmptyList_WhenNoCinemas() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Collections.emptyList());

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(cinemaService, times(1)).getAllActiveCinemas();
    }

    @Test
    @DisplayName("getAllActiveCinemas - Retorna información completa de los cines")
    void getAllActiveCinemas_ReturnsCompleteInfo() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Arrays.asList(mockCinema1));

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        CinemaDTO cinema = response.getBody().get(0);
        assertAll("Cinema info",
            () -> assertEquals(1L, cinema.getId()),
            () -> assertEquals("Cinema Test 1", cinema.getName()),
            () -> assertEquals("Calle Test 123", cinema.getAddress()),
            () -> assertEquals("Buenos Aires", cinema.getCity()),
            () -> assertEquals("123456789", cinema.getPhone())
        );
    }

    @Test
    @DisplayName("getAllActiveCinemas - Solo retorna cines activos")
    void getAllActiveCinemas_OnlyReturnsActive() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Arrays.asList(mockCinema1, mockCinema2));

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        assertEquals(2, response.getBody().size());
        verify(cinemaService, times(1)).getAllActiveCinemas();
        verify(cinemaService, never()).getAllCinemas();
    }

    @Test
    @DisplayName("getCinemaById - Retorna cine por ID")
    void getCinemaById_ReturnsCinema() {
        when(cinemaService.getCinemaById(1L)).thenReturn(mockCinema1);

        ResponseEntity<CinemaDTO> response = cinemaController.getCinemaById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Cinema Test 1", response.getBody().getName());

        verify(cinemaService, times(1)).getCinemaById(1L);
    }

    @Test
    @DisplayName("getCinemaById - Retorna información completa del cine")
    void getCinemaById_ReturnsCompleteInfo() {
        when(cinemaService.getCinemaById(1L)).thenReturn(mockCinema1);

        ResponseEntity<CinemaDTO> response = cinemaController.getCinemaById(1L);

        CinemaDTO cinema = response.getBody();
        assertNotNull(cinema);
        assertAll("Cinema details",
            () -> assertNotNull(cinema.getId()),
            () -> assertNotNull(cinema.getName()),
            () -> assertNotNull(cinema.getAddress()),
            () -> assertNotNull(cinema.getCity()),
            () -> assertNotNull(cinema.getPhone())
        );
    }

    @Test
    @DisplayName("getCinemaById - Lanza excepción cuando ID no existe")
    void getCinemaById_ThrowsException_WhenIdNotFound() {
        when(cinemaService.getCinemaById(999L))
                .thenThrow(new RuntimeException("Cine no encontrado con ID: 999"));

        assertThrows(RuntimeException.class, () -> {
            cinemaController.getCinemaById(999L);
        });

        verify(cinemaService, times(1)).getCinemaById(999L);
    }

    @Test
    @DisplayName("getCinemaById - Retorna diferentes cines según ID")
    void getCinemaById_ReturnsDifferentCinemas() {
        when(cinemaService.getCinemaById(1L)).thenReturn(mockCinema1);
        when(cinemaService.getCinemaById(2L)).thenReturn(mockCinema2);

        ResponseEntity<CinemaDTO> response1 = cinemaController.getCinemaById(1L);
        ResponseEntity<CinemaDTO> response2 = cinemaController.getCinemaById(2L);

        assertNotEquals(response1.getBody().getId(), response2.getBody().getId());
        assertNotEquals(response1.getBody().getName(), response2.getBody().getName());
        assertNotEquals(response1.getBody().getCity(), response2.getBody().getCity());

        verify(cinemaService, times(1)).getCinemaById(1L);
        verify(cinemaService, times(1)).getCinemaById(2L);
    }

    @Test
    @DisplayName("getAllActiveCinemas - Mantiene el orden de la base de datos")
    void getAllActiveCinemas_MaintainsDatabaseOrder() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Arrays.asList(mockCinema2, mockCinema1));

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        List<CinemaDTO> cinemas = response.getBody();
        assertEquals("Cinema Test 2", cinemas.get(0).getName());
        assertEquals("Cinema Test 1", cinemas.get(1).getName());
    }

    @Test
    @DisplayName("getCinemaById - Verifica que se llame al servicio con el ID correcto")
    void getCinemaById_CallsServiceWithCorrectId() {
        when(cinemaService.getCinemaById(5L)).thenReturn(mockCinema1);

        cinemaController.getCinemaById(5L);

        verify(cinemaService, times(1)).getCinemaById(5L);
        verify(cinemaService, never()).getCinemaById(1L);
        verify(cinemaService, never()).getAllActiveCinemas();
    }

    @Test
    @DisplayName("getAllActiveCinemas - Retorna cines de diferentes ciudades")
    void getAllActiveCinemas_ReturnsCinemasFromDifferentCities() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Arrays.asList(mockCinema1, mockCinema2));

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        List<CinemaDTO> cinemas = response.getBody();
        assertEquals("Buenos Aires", cinemas.get(0).getCity());
        assertEquals("Córdoba", cinemas.get(1).getCity());
    }

    @Test
    @DisplayName("getCinemaById - Retorna teléfono del cine")
    void getCinemaById_ReturnsPhoneNumber() {
        when(cinemaService.getCinemaById(1L)).thenReturn(mockCinema1);

        ResponseEntity<CinemaDTO> response = cinemaController.getCinemaById(1L);

        assertEquals("123456789", response.getBody().getPhone());
    }

    @Test
    @DisplayName("getAllActiveCinemas - Verifica llamada única al servicio")
    void getAllActiveCinemas_CallsServiceOnce() {
        when(cinemaService.getAllActiveCinemas()).thenReturn(Arrays.asList(mockCinema1));

        cinemaController.getAllActiveCinemas();

        verify(cinemaService, times(1)).getAllActiveCinemas();
        verifyNoMoreInteractions(cinemaService);
    }

    @Test
    @DisplayName("getCinemaById - Verifica llamada única al servicio")
    void getCinemaById_CallsServiceOnce() {
        when(cinemaService.getCinemaById(1L)).thenReturn(mockCinema1);

        cinemaController.getCinemaById(1L);

        verify(cinemaService, times(1)).getCinemaById(1L);
        verifyNoMoreInteractions(cinemaService);
    }

    @Test
    @DisplayName("getAllActiveCinemas - Retorna múltiples cines correctamente")
    void getAllActiveCinemas_ReturnsMultipleCinemasCorrectly() {
        CinemaDTO cinema3 = new CinemaDTO();
        cinema3.setId(3L);
        cinema3.setName("Cinema Test 3");
        cinema3.setCity("Rosario");

        when(cinemaService.getAllActiveCinemas())
                .thenReturn(Arrays.asList(mockCinema1, mockCinema2, cinema3));

        ResponseEntity<List<CinemaDTO>> response = cinemaController.getAllActiveCinemas();

        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().stream().anyMatch(c -> c.getCity().equals("Rosario")));
    }
}

