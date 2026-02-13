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
import com.cinebook.backend.repositories.WeeklyScheduleRepository;
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
    private final WeeklyScheduleRepository weeklyScheduleRepository;
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
                .filter(showtime -> showtime.getMovie().getIsActive())
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
                .filter(showtime -> showtime.getMovie().getIsActive())
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
                .filter(showtime -> showtime.getMovie().getIsActive())
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
                .filter(showtime -> showtime.getMovie().getIsActive())
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getShowDateTime().compareTo(b.getShowDateTime()))
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
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(6);

        log.info("🎬 Generando funciones desde {} hasta {}", today, endDate);

        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();

        List<Movie> activeMovies = movieRepository.findByIsActiveTrueOrderByIdAsc();
        List<Cinema> cinemas = cinemaRepository.findByIsActiveTrue();

        if (activeMovies.isEmpty()) {
            log.warn("⚠️ No hay películas activas");
            return;
        }

        if (cinemas.isEmpty()) {
            log.warn("⚠️ No hay cines disponibles");
            return;
        }

        int totalGenerated = 0;

        for (Movie movie : activeMovies) {
            for (Cinema cinema : cinemas) {
                for (LocalDate date = today; !date.isAfter(endDate); date = date.plusDays(1)) {
                    final LocalDate currentDate = date;

                    List<Showtime> existing = showtimeRepository.findAll().stream()
                        .filter(s -> s.getMovie().getId().equals(movie.getId()))
                        .filter(s -> s.getCinema().getId().equals(cinema.getId()))
                        .filter(s -> s.getShowDateTime().toLocalDate().equals(currentDate))
                        .collect(Collectors.toList());

                    if (existing.isEmpty()) {
                        List<Showtime> dailyShowtimes = generateDailyShowtimes(movie, cinema, currentDate, currentWeek);
                        showtimeRepository.saveAll(dailyShowtimes);
                        totalGenerated += dailyShowtimes.size();
                    }
                }
            }
        }

        log.info("✅ {} funciones generadas para {} películas en {} cines",
            totalGenerated, activeMovies.size(), cinemas.size());
    }

    @Override
    @Transactional
    public void generateShowtimesForDate(LocalDate date) {
        log.info("🎬 Generando funciones para la fecha: {}", date);

        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();

        List<Movie> activeMovies = movieRepository.findByIsActiveTrueOrderByIdAsc();
        List<Cinema> cinemas = cinemaRepository.findByIsActiveTrue();

        if (activeMovies.isEmpty()) {
            log.warn("⚠️ No hay películas activas");
            return;
        }

        if (cinemas.isEmpty()) {
            log.warn("⚠️ No hay cines disponibles");
            return;
        }

        int totalGenerated = 0;

        for (Movie movie : activeMovies) {
            for (Cinema cinema : cinemas) {
                List<Showtime> existing = showtimeRepository.findAll().stream()
                    .filter(s -> s.getMovie().getId().equals(movie.getId()))
                    .filter(s -> s.getCinema().getId().equals(cinema.getId()))
                    .filter(s -> s.getShowDateTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());

                if (existing.isEmpty()) {
                    List<Showtime> dailyShowtimes = generateDailyShowtimes(movie, cinema, date, currentWeek);
                    showtimeRepository.saveAll(dailyShowtimes);
                    totalGenerated += dailyShowtimes.size();
                }
            }
        }

        log.info("✅ {} funciones generadas para {} películas en {} cines para la fecha {}",
            totalGenerated, activeMovies.size(), cinemas.size(), date);
    }


    private List<Showtime> generateDailyShowtimes(Movie movie, Cinema cinema, LocalDate date, WeeklySchedule week) {
        List<Showtime> showtimes = new ArrayList<>();

        Showtime showtime1 = createShowtime(
                movie, cinema, date,
                LocalTime.of(17, 30),
                ShowtimeType.SPANISH_2D,
                new BigDecimal("5000"),
                week.getWeekId()
        );
        showtimes.add(showtime1);

        boolean is3D = (date.getDayOfMonth() % 2 == 0);

        Showtime showtime2 = createShowtime(
                movie, cinema, date,
                LocalTime.of(21, 0),
                is3D ? ShowtimeType.SPANISH_3D : ShowtimeType.SUBTITLED_2D,
                is3D ? new BigDecimal("8000") : new BigDecimal("4500"),
                week.getWeekId()
        );
        showtimes.add(showtime2);

        return showtimes;
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

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeDTO> getShowtimesByFilters(Long movieId, Long cinemaId, LocalDate date) {
        log.info("Filtrando funciones: película {}, cine {}, fecha {}", movieId, cinemaId, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        LocalDateTime now = LocalDateTime.now();

        log.info("⏰ Rango de búsqueda: {} a {}", startOfDay, endOfDay);
        log.info("⏰ Hora actual: {}", now);

        List<Showtime> allShowtimes = showtimeRepository.findByCinemaIdAndMovieId(cinemaId, movieId);
        log.info("📊 Total funciones en BD para cine {} y película {}: {}", cinemaId, movieId, allShowtimes.size());

        List<ShowtimeDTO> result = allShowtimes.stream()
                .filter(showtime -> {
                    LocalDateTime showDateTime = showtime.getShowDateTime();
                    boolean isInDateRange = showDateTime.isAfter(startOfDay) && showDateTime.isBefore(endOfDay);
                    boolean isFuture = showDateTime.isAfter(now);
                    boolean isActive = showtime.getMovie() != null && showtime.getMovie().getIsActive();

                    log.debug("  Showtime {}: dateTime={}, inRange={}, future={}, active={}",
                            showtime.getId(), showDateTime, isInDateRange, isFuture, isActive);

                    return isInDateRange && isFuture && isActive;
                })
                .map(this::convertToDTO)
                .sorted((a, b) -> a.getShowDateTime().compareTo(b.getShowDateTime()))
                .collect(Collectors.toList());

        log.info("✅ Funciones válidas encontradas: {}", result.size());

        return result;
    }
}