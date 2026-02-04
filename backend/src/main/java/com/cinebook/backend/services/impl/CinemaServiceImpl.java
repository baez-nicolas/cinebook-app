package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.CinemaDTO;
import com.cinebook.backend.entities.Cinema;
import com.cinebook.backend.repositories.CinemaRepository;
import com.cinebook.backend.services.interfaces.ICinemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CinemaServiceImpl implements ICinemaService {

    private final CinemaRepository cinemaRepository;

    @Override
    public List<CinemaDTO> getAllActiveCinemas() {
        log.info("Obteniendo todos los cines activos");
        return cinemaRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CinemaDTO getCinemaById(Long id) {
        log.info("Obteniendo cine con ID: {}", id);
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cine no encontrado con ID: " + id));
        return convertToDTO(cinema);
    }

    @Override
    public List<CinemaDTO> getAllCinemas() {
        log.info("Obteniendo todos los cines");
        return cinemaRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CinemaDTO convertToDTO(Cinema cinema) {
        CinemaDTO dto = new CinemaDTO();
        dto.setId(cinema.getId());
        dto.setName(cinema.getName());
        dto.setAddress(cinema.getAddress());
        dto.setCity(cinema.getCity());
        dto.setPhone(cinema.getPhone());
        return dto;
    }
}