package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.MovieDTO;
import com.cinebook.backend.entities.Movie;

import java.util.List;

public interface IMovieService {
    List<MovieDTO> getAllActiveMovies();
    MovieDTO getMovieById(Long id);
    List<MovieDTO> getAllMovies();
    MovieDTO convertToDTO(Movie movie);
}