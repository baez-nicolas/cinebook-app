package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.CinemaDTO;
import com.cinebook.backend.entities.Cinema;

import java.util.List;

public interface ICinemaService {
    List<CinemaDTO> getAllActiveCinemas();
    CinemaDTO getCinemaById(Long id);
    List<CinemaDTO> getAllCinemas();
    CinemaDTO convertToDTO(Cinema cinema);
}