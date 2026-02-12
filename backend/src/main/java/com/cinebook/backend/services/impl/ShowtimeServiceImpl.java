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

        LocalTime[] showtimes2DSpanish = {
                LocalTime.of(16, 0),
                LocalTime.of(21, 0)
        };

        LocalTime[] showtimes2DSubtitled = {
                LocalTime.of(18, 30)
        };

        LocalTime[] showtimes3D = {
                LocalTime.of(15, 0),
                LocalTime.of(22, 30)
        };

        Map<ShowtimeType, BigDecimal> prices = new EnumMap<>(ShowtimeType.class);
        prices.put(ShowtimeType.SPANISH_2D, new BigDecimal("3500"));
        prices.put(ShowtimeType.SUBTITLED_2D, new BigDecimal("3800"));
        prices.put(ShowtimeType.SPANISH_3D, new BigDecimal("4500"));

        List<Showtime> allShowtimes = new ArrayList<>();
        int functionCount = 0;
        Random random = new Random();

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate currentDate = currentWeek.getWeekStartDate().plusDays(dayOffset);

            for (Cinema cinema : cinemas) {
                List<Movie> shuffledMovies = new ArrayList<>(movies);
                Collections.shuffle(shuffledMovies, random);

                List<Movie> moviesForThisCinema = shuffledMovies.subList(0, Math.min(3, shuffledMovies.size()));

                for (Movie movie : moviesForThisCinema) {

                    for (LocalTime time : showtimes2DSpanish) {
                        Showtime showtime = createShowtime(
                                movie, cinema, currentDate, time,
                                ShowtimeType.SPANISH_2D,
                                prices.get(ShowtimeType.SPANISH_2D),
                                currentWeek.getWeekId()
                        );
                        allShowtimes.add(showtime);
                        functionCount++;
                    }

                    for (LocalTime time : showtimes2DSubtitled) {
                        Showtime showtime = createShowtime(
                                movie, cinema, currentDate, time,
                                ShowtimeType.SUBTITLED_2D,
                                prices.get(ShowtimeType.SUBTITLED_2D),
                                currentWeek.getWeekId()
                        );
                        allShowtimes.add(showtime);
                        functionCount++;
                    }

                    for (LocalTime time : showtimes3D) {
                        Showtime showtime = createShowtime(
                                movie, cinema, currentDate, time,
                                ShowtimeType.SPANISH_3D,
                                prices.get(ShowtimeType.SPANISH_3D),
                                currentWeek.getWeekId()
                        );
                        allShowtimes.add(showtime);
                        functionCount++;
                    }
                }
            }
        }

        showtimeRepository.saveAll(allShowtimes);
        log.info("✅ {} funciones generadas exitosamente para la semana {}", functionCount, currentWeek.getWeekId());

        int expectedFunctions = 3 * cinemas.size() * 7 *
                (showtimes2DSpanish.length + showtimes2DSubtitled.length + showtimes3D.length);
        log.info("📊 Esperadas: {} | Generadas: {}", expectedFunctions, functionCount);
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