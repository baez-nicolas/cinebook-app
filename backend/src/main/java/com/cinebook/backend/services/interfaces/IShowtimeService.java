package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.ShowtimeDTO;
import com.cinebook.backend.entities.Showtime;

import java.util.List;

public interface IShowtimeService {
    List<ShowtimeDTO> getCurrentWeekShowtimes();
    List<ShowtimeDTO> getShowtimesByMovie(Long movieId);
    List<ShowtimeDTO> getShowtimesByCinema(Long cinemaId);
    List<ShowtimeDTO> getShowtimesByCinemaAndMovie(Long cinemaId, Long movieId);
    ShowtimeDTO getShowtimeById(Long id);
    void generateShowtimesForCurrentWeek();
    ShowtimeDTO convertToDTO(Showtime showtime);
}