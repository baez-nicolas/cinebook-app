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
        Long weekIdLong = currentWeek.getWeekId();

        List<Showtime> existingShowtimes = showtimeRepository.findByWeekId(weekIdLong);
        if (!existingShowtimes.isEmpty()) {
            log.info("Ya existen {} funciones para la semana {}", existingShowtimes.size(), weekIdLong);
            return;
        }

        log.info("🎬 Generando funciones para la semana {}...", weekIdLong);

        List<Movie> movies = movieRepository.findByIsActiveTrue();
        List<Cinema> cinemas = cinemaRepository.findByIsActiveTrue();

        if (movies.isEmpty() || cinemas.isEmpty()) {
            log.warn("No hay películas o cines activos para generar funciones");
            return;
        }

        Map<Long, List<ShowtimeType>> movieTypes = generateRandomTypesForMovies(movies);

        int totalGenerated = 0;

        for (Cinema cinema : cinemas) {
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate currentDate = currentWeek.getWeekStartDate().plusDays(dayOffset);
                totalGenerated += generateShowtimesForCinemaDay(cinema, movies, movieTypes, currentDate, weekIdLong);
            }
        }

        log.info("✅ Se generaron {} funciones para {} cines en 7 días", totalGenerated, cinemas.size());
    }

    private int generateShowtimesForCinemaDay(Cinema cinema, List<Movie> movies, Map<Long, List<ShowtimeType>> movieTypes, LocalDate date, Long weekId) {
        Random random = new Random();
        int generated = 0;

        List<LocalTime> timeSlots = Arrays.asList(
                LocalTime.of(13, 30),
                LocalTime.of(16, 30),
                LocalTime.of(19, 30),
                LocalTime.of(22, 30)
        );

        int salasUsadas = 0;

        for (Movie movie : movies) {
            if (salasUsadas >= 18) break;

            LocalTime time = timeSlots.get(random.nextInt(timeSlots.size()));
            LocalDateTime showDateTime = LocalDateTime.of(date, time);

            List<ShowtimeType> availableTypes = movieTypes.get(movie.getId());
            ShowtimeType type = availableTypes.get(random.nextInt(availableTypes.size()));

            Showtime showtime = new Showtime();
            showtime.setMovie(movie);
            showtime.setCinema(cinema);
            showtime.setShowDateTime(showDateTime);
            showtime.setType(type);
            showtime.setPrice(calculatePrice(type));
            showtime.setWeekId(weekId);
            showtime.setTotalSeats(30);
            showtime.setAvailableSeats(30);

            showtimeRepository.save(showtime);
            generated++;
            salasUsadas++;
        }

        while (salasUsadas < 18) {
            Movie movie = movies.get(random.nextInt(movies.size()));
            LocalTime time = timeSlots.get(random.nextInt(timeSlots.size()));
            LocalDateTime showDateTime = LocalDateTime.of(date, time);

            List<ShowtimeType> availableTypes = movieTypes.get(movie.getId());
            ShowtimeType type = availableTypes.get(random.nextInt(availableTypes.size()));

            Showtime showtime = new Showtime();
            showtime.setMovie(movie);
            showtime.setCinema(cinema);
            showtime.setShowDateTime(showDateTime);
            showtime.setType(type);
            showtime.setPrice(calculatePrice(type));
            showtime.setWeekId(weekId);
            showtime.setTotalSeats(30);
            showtime.setAvailableSeats(30);

            showtimeRepository.save(showtime);
            generated++;
            salasUsadas++;
        }

        return generated;
    }

    private Map<Long, List<ShowtimeType>> generateRandomTypesForMovies(List<Movie> movies) {
        Map<Long, List<ShowtimeType>> movieTypes = new HashMap<>();
        Random random = new Random();

        for (Movie movie : movies) {
            List<ShowtimeType> types = new ArrayList<>();

            types.add(ShowtimeType.SPANISH_2D);

            if (random.nextBoolean()) {
                types.add(ShowtimeType.SUBTITLED_2D);
            }

            if (random.nextBoolean()) {
                types.add(ShowtimeType.SPANISH_3D);
            }

            movieTypes.put(movie.getId(), types);

            log.info("📽️ {} - Tipos disponibles: {}", movie.getTitle(), types);
        }

        return movieTypes;
    }

    private BigDecimal calculatePrice(ShowtimeType type) {
        return switch (type) {
            case SPANISH_2D -> new BigDecimal("1500");
            case SUBTITLED_2D -> new BigDecimal("1700");
            case SPANISH_3D -> new BigDecimal("2000");
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