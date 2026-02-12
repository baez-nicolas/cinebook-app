package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.ShowtimeDTO;
import com.cinebook.backend.entities.Cinema;
import com.cinebook.backend.entities.Movie;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.cinebook.backend.repositories.CinemaRepository;
import com.cinebook.backend.repositories.MovieRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowtimeServiceImpl implements IShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final IWeeklyScheduleService weeklyScheduleService;

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getCurrentWeekShowtimes() {
        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();
        log.info("Obteniendo funciones de la semana actual (weekId: {})", currentWeek.getWeekId());

        LocalDateTime now = LocalDateTime.now();

        return showtimeRepository.findByWeekId(currentWeek.getWeekId())
                .stream()
                .filter(showtime -> showtime.getShowDateTime().isAfter(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByMovie(Long movieId) {
        log.info("Obteniendo funciones de la película ID: {}", movieId);

        LocalDateTime now = LocalDateTime.now();

        return showtimeRepository.findByMovieId(movieId)
                .stream()
                .filter(showtime -> showtime.getShowDateTime().isAfter(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByCinema(Long cinemaId) {
        log.info("Obteniendo funciones del cine ID: {}", cinemaId);

        LocalDateTime now = LocalDateTime.now();

        return showtimeRepository.findByCinemaId(cinemaId)
                .stream()
                .filter(showtime -> showtime.getShowDateTime().isAfter(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByCinemaAndMovie(Long cinemaId, Long movieId) {
        log.info("Obteniendo funciones del cine {} y película {}", cinemaId, movieId);

        LocalDateTime now = LocalDateTime.now();

        return showtimeRepository.findByCinemaIdAndMovieId(cinemaId, movieId)
                .stream()
                .filter(showtime -> showtime.getShowDateTime().isAfter(now))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShowtimeDTO getShowtimeById(Long id) {
        log.info("Obteniendo función con ID: {}", id);
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Función no encontrada con ID: " + id));
        return convertToDTO(showtime);
    }

    @Override
    @Transactional
    public void generateShowtimesForCurrentWeek() {
        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();

        List<Showtime> existingShowtimes = showtimeRepository.findByWeekId(currentWeek.getWeekId());
        if (!existingShowtimes.isEmpty()) {
            log.info("Las funciones ya fueron generadas para la semana actual (weekId: {})", currentWeek.getWeekId());
            return;
        }

        log.info("🎬 Generando funciones para la semana {} ({} - {})",
                currentWeek.getWeekId(),
                currentWeek.getWeekStartDate(),
                currentWeek.getWeekEndDate());

        List<Movie> movies = movieRepository.findByIsActiveTrue();
        List<Cinema> cinemas = cinemaRepository.findByIsActiveTrue();

        if (movies.isEmpty() || cinemas.isEmpty()) {
            log.warn("No hay películas o cines activos para generar funciones");
            return;
        }

        List<Showtime> allShowtimes = new ArrayList<>();
        int functionCount = 0;

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate currentDate = currentWeek.getWeekStartDate().plusDays(dayOffset);

            for (Cinema cinema : cinemas) {
                for (Movie movie : movies) {

                    Showtime showtime1 = createShowtime(
                            movie, cinema, currentDate,
                            LocalTime.of(17, 30),
                            ShowtimeType.SPANISH_2D,
                            new BigDecimal("5000"),
                            currentWeek.getWeekId()
                    );
                    allShowtimes.add(showtime1);
                    functionCount++;

                    boolean is3D = (dayOffset % 2 == 0);

                    Showtime showtime2 = createShowtime(
                            movie, cinema, currentDate,
                            LocalTime.of(21, 0),
                            is3D ? ShowtimeType.SPANISH_3D : ShowtimeType.SUBTITLED_2D,
                            is3D ? new BigDecimal("8000") : new BigDecimal("4500"),
                            currentWeek.getWeekId()
                    );
                    allShowtimes.add(showtime2);
                    functionCount++;
                }
            }
        }

        showtimeRepository.saveAll(allShowtimes);

        log.info("✅ {} funciones generadas exitosamente para la semana {}", functionCount, currentWeek.getWeekId());
        log.info("📊 Distribución:");
        log.info("   • Películas: {}", movies.size());
        log.info("   • Cines: {}", cinemas.size());
        log.info("   • Días: 7");
        log.info("   • Funciones por película/cine/día: 2");
        log.info("   • Total esperado: {}", movies.size() * cinemas.size() * 7 * 2);
    }

    private Showtime createShowtime(Movie movie, Cinema cinema, LocalDate date, LocalTime time,
                                     ShowtimeType type, BigDecimal price, Long weekId) {
        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setCinema(cinema);
        showtime.setShowDateTime(LocalDateTime.of(date, time));
        showtime.setType(type);
        showtime.setPrice(price);
        showtime.setWeekId(weekId);
        return showtime;
    }

    @Override
    public ShowtimeDTO convertToDTO(Showtime showtime) {
        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setId(showtime.getId());
        dto.setMovieId(showtime.getMovie().getId());
        dto.setMovieTitle(showtime.getMovie().getTitle());
        dto.setMoviePosterUrl(showtime.getMovie().getPosterUrl());
        dto.setCinemaId(showtime.getCinema().getId());
        dto.setCinemaName(showtime.getCinema().getName());
        dto.setShowDateTime(showtime.getShowDateTime());
        dto.setType(showtime.getType());
        dto.setPrice(showtime.getPrice());
        dto.setAvailableSeats(showtime.getAvailableSeats());
        dto.setTotalSeats(showtime.getTotalSeats());
        return dto;
    }
}