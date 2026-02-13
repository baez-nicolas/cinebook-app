package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.CinemaDTO;
import com.cinebook.backend.entities.Cinema;
import com.cinebook.backend.repositories.CinemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CinemaServiceImpl Tests")
class CinemaServiceImplTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @InjectMocks
    private CinemaServiceImpl cinemaService;

    private Cinema mockCinema1;
    private Cinema mockCinema2;
    private Cinema mockCinema3;

    @BeforeEach
    void setUp() {
        mockCinema1 = new Cinema();
        mockCinema1.setId(1L);
        mockCinema1.setName("Cine Centro");
        mockCinema1.setAddress("Av. Principal 123");
        mockCinema1.setCity("Buenos Aires");
        mockCinema1.setPhone("1234-5678");
        mockCinema1.setIsActive(true);

        mockCinema2 = new Cinema();
        mockCinema2.setId(2L);
        mockCinema2.setName("Cine Norte");
        mockCinema2.setAddress("Calle Norte 456");
        mockCinema2.setCity("Córdoba");
        mockCinema2.setPhone("2345-6789");
        mockCinema2.setIsActive(true);

        mockCinema3 = new Cinema();
        mockCinema3.setId(3L);
        mockCinema3.setName("Cine Cerrado");
        mockCinema3.setAddress("Av. Cerrada 789");
        mockCinema3.setCity("Rosario");
        mockCinema3.setPhone("3456-7890");
        mockCinema3.setIsActive(false);
    }

    @Test
    @DisplayName("getAllActiveCinemas - Retorna solo cines activos")
    void getAllActiveCinemas_ReturnsOnlyActiveCinemas() {
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockCinema1, mockCinema2));

        List<CinemaDTO> result = cinemaService.getAllActiveCinemas();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cine Centro", result.get(0).getName());
        assertEquals("Cine Norte", result.get(1).getName());

        verify(cinemaRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAllActiveCinemas - Lista vacía cuando no hay cines activos")
    void getAllActiveCinemas_EmptyList_WhenNoActiveCinemas() {
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        List<CinemaDTO> result = cinemaService.getAllActiveCinemas();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(cinemaRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAllActiveCinemas - Convierte correctamente a DTO")
    void getAllActiveCinemas_ConvertsToDTO_Correctly() {
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockCinema1));

        List<CinemaDTO> result = cinemaService.getAllActiveCinemas();

        assertNotNull(result);
        assertEquals(1, result.size());
        CinemaDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Cine Centro", dto.getName());
        assertEquals("Av. Principal 123", dto.getAddress());
        assertEquals("Buenos Aires", dto.getCity());
        assertEquals("1234-5678", dto.getPhone());

        verify(cinemaRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getCinemaById - Retorna cine cuando existe")
    void getCinemaById_Found_ReturnsDTO() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(mockCinema1));

        CinemaDTO result = cinemaService.getCinemaById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cine Centro", result.getName());
        assertEquals("Av. Principal 123", result.getAddress());
        assertEquals("Buenos Aires", result.getCity());
        assertEquals("1234-5678", result.getPhone());

        verify(cinemaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getCinemaById - Lanza excepción cuando no existe")
    void getCinemaById_NotFound_ThrowsException() {
        when(cinemaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cinemaService.getCinemaById(999L);
        });

        assertEquals("Cine no encontrado con ID: 999", exception.getMessage());

        verify(cinemaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getCinemaById - Retorna cine inactivo si se busca por ID")
    void getCinemaById_ReturnsInactiveCinema() {
        when(cinemaRepository.findById(3L)).thenReturn(Optional.of(mockCinema3));

        CinemaDTO result = cinemaService.getCinemaById(3L);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("Cine Cerrado", result.getName());

        verify(cinemaRepository, times(1)).findById(3L);
    }

    @Test
    @DisplayName("getAllCinemas - Retorna todos los cines (activos e inactivos)")
    void getAllCinemas_ReturnsAllCinemas() {
        when(cinemaRepository.findAll()).thenReturn(Arrays.asList(mockCinema1, mockCinema2, mockCinema3));

        List<CinemaDTO> result = cinemaService.getAllCinemas();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Cine Centro", result.get(0).getName());
        assertEquals("Cine Norte", result.get(1).getName());
        assertEquals("Cine Cerrado", result.get(2).getName());

        verify(cinemaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllCinemas - Lista vacía cuando no hay cines")
    void getAllCinemas_EmptyList_WhenNoCinemas() {
        when(cinemaRepository.findAll()).thenReturn(Collections.emptyList());

        List<CinemaDTO> result = cinemaService.getAllCinemas();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(cinemaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("convertToDTO - Convierte todos los campos correctamente")
    void convertToDTO_ConvertsAllFieldsCorrectly() {
        CinemaDTO result = cinemaService.convertToDTO(mockCinema1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cine Centro", result.getName());
        assertEquals("Av. Principal 123", result.getAddress());
        assertEquals("Buenos Aires", result.getCity());
        assertEquals("1234-5678", result.getPhone());
    }

    @Test
    @DisplayName("convertToDTO - Maneja valores nulos correctamente")
    void convertToDTO_HandlesNullValues() {
        Cinema cinemaWithNulls = new Cinema();
        cinemaWithNulls.setId(4L);
        cinemaWithNulls.setName("Cine Sin Datos");
        cinemaWithNulls.setAddress(null);
        cinemaWithNulls.setCity(null);
        cinemaWithNulls.setPhone(null);

        CinemaDTO result = cinemaService.convertToDTO(cinemaWithNulls);

        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("Cine Sin Datos", result.getName());
        assertNull(result.getAddress());
        assertNull(result.getCity());
        assertNull(result.getPhone());
    }

    @Test
    @DisplayName("getAllActiveCinemas - Mantiene el orden de la base de datos")
    void getAllActiveCinemas_MaintainsDatabaseOrder() {
        when(cinemaRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(mockCinema2, mockCinema1));

        List<CinemaDTO> result = cinemaService.getAllActiveCinemas();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cine Norte", result.get(0).getName());
        assertEquals("Cine Centro", result.get(1).getName());

        verify(cinemaRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getAllCinemas - Incluye cines activos e inactivos")
    void getAllCinemas_IncludesActiveAndInactiveCinemas() {
        when(cinemaRepository.findAll()).thenReturn(Arrays.asList(mockCinema1, mockCinema3));

        List<CinemaDTO> result = cinemaService.getAllCinemas();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Cine Centro")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Cine Cerrado")));

        verify(cinemaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getCinemaById - Llama al repositorio con el ID correcto")
    void getCinemaById_CallsRepositoryWithCorrectId() {
        when(cinemaRepository.findById(2L)).thenReturn(Optional.of(mockCinema2));

        cinemaService.getCinemaById(2L);

        verify(cinemaRepository, times(1)).findById(2L);
        verify(cinemaRepository, never()).findById(1L);
        verify(cinemaRepository, never()).findAll();
    }

    @Test
    @DisplayName("convertToDTO - No modifica la entidad original")
    void convertToDTO_DoesNotModifyOriginalEntity() {
        Cinema original = new Cinema();
        original.setId(5L);
        original.setName("Original Name");
        original.setAddress("Original Address");
        original.setCity("Original City");
        original.setPhone("Original Phone");

        CinemaDTO dto = cinemaService.convertToDTO(original);

        assertEquals("Original Name", original.getName());
        assertEquals("Original Address", original.getAddress());
        assertEquals("Original Name", dto.getName());
        assertEquals("Original Address", dto.getAddress());
    }
}

