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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowtimeServiceImpl implements IShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final IWeeklyScheduleService weeklyScheduleService;

    private final Random random = new Random();

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getCurrentWeekShowtimes() {
        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();
        log.info("Obteniendo funciones de la semana actual (weekId: {})", currentWeek.getWeekId());

        return showtimeRepository.findByWeekId(currentWeek.getWeekId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByMovie(Long movieId) {
        log.info("Obteniendo funciones de la película ID: {}", movieId);
        return showtimeRepository.findByMovieId(movieId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByCinema(Long cinemaId) {
        log.info("Obteniendo funciones del cine ID: {}", cinemaId);
        return showtimeRepository.findByCinemaId(cinemaId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByCinemaAndMovie(Long cinemaId, Long movieId) {
        log.info("Obteniendo funciones del cine {} y película {}", cinemaId, movieId);
        return showtimeRepository.findByCinemaIdAndMovieId(cinemaId, movieId)
                .stream()
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
        Long weekId = currentWeek.getWeekId();

        log.info("🎬 Generando funciones para la semana {} ({} a {})",
                weekId, currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());

        List<Showtime> existing = showtimeRepository.findByWeekId(weekId);
        if (!existing.isEmpty()) {
            log.info("⚠️ Ya existen {} funciones para esta semana. Saltando generación.", existing.size());
            return;
        }

        List<Cinema> cinemas = cinemaRepository.findByIsActiveTrue();

        List<Movie> movies = movieRepository.findByIsActiveTrue();

        if (cinemas.isEmpty() || movies.isEmpty()) {
            log.warn("⚠️ No hay cines o películas activas. No se pueden generar funciones.");
            return;
        }

        int totalShowtimes = 0;

        for (Cinema cinema : cinemas) {
            log.info("Generando funciones para: {}", cinema.getName());

            for (int i = 0; i < 6; i++) {
                Showtime showtime = generateRandomShowtime(cinema, movies, weekId, currentWeek);
                showtimeRepository.save(showtime);
                totalShowtimes++;
            }
        }

        log.info("✅ Se generaron {} funciones para {} cines", totalShowtimes, cinemas.size());
    }

    private Showtime generateRandomShowtime(Cinema cinema, List<Movie> movies,
                                            Long weekId, WeeklySchedule currentWeek) {
        Showtime showtime = new Showtime();

        showtime.setCinema(cinema);

        Movie randomMovie = movies.get(random.nextInt(movies.size()));
        showtime.setMovie(randomMovie);

        LocalDateTime randomDateTime = generateRandomDateTime(
                currentWeek.getWeekStartDate(),
                currentWeek.getWeekEndDate()
        );
        showtime.setShowDateTime(randomDateTime);

        ShowtimeType randomType = getRandomShowtimeType();
        showtime.setType(randomType);

        BigDecimal price = calculatePrice(randomType);
        showtime.setPrice(price);

        showtime.setTotalSeats(30);
        showtime.setAvailableSeats(30);

        showtime.setWeekId(weekId);

        return showtime;
    }

    private LocalDateTime generateRandomDateTime(LocalDate startDate, LocalDate endDate) {
        long daysBetween = startDate.until(endDate).getDays();
        int randomDays = random.nextInt((int) daysBetween + 1);
        LocalDate randomDate = startDate.plusDays(randomDays);

        int[] possibleHours = {14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        int randomHour = possibleHours[random.nextInt(possibleHours.length)];

        int[] possibleMinutes = {0, 15, 30, 45};
        int randomMinute = possibleMinutes[random.nextInt(possibleMinutes.length)];

        LocalTime randomTime = LocalTime.of(randomHour, randomMinute);

        return LocalDateTime.of(randomDate, randomTime);
    }

    private ShowtimeType getRandomShowtimeType() {
        ShowtimeType[] types = ShowtimeType.values();
        return types[random.nextInt(types.length)];
    }

    private BigDecimal calculatePrice(ShowtimeType type) {
        return switch (type) {
            case SPANISH_2D -> BigDecimal.valueOf(1500);         // $1500
            case SUBTITLED_2D -> BigDecimal.valueOf(1500);    // $1500
            case SPANISH_3D -> BigDecimal.valueOf(2000);      // $2000
        };
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