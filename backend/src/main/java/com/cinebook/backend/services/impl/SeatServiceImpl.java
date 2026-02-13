package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.SeatDTO;
import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.entities.enums.SeatStatus;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.services.interfaces.ISeatService;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatServiceImpl implements ISeatService {

    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final IWeeklyScheduleService weeklyScheduleService;

    @Override
    public List<SeatDTO> getSeatsByShowtime(Long showtimeId) {
        log.info("Obteniendo asientos de la función ID: {}", showtimeId);
        return seatRepository.findByShowtimeId(showtimeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getAvailableSeatsByShowtime(Long showtimeId) {
        log.info("Obteniendo asientos disponibles de la función ID: {}", showtimeId);
        return seatRepository.findByShowtimeIdAndStatus(showtimeId, SeatStatus.AVAILABLE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void generateSeatsForShowtime(Showtime showtime) {
        List<Seat> existingSeats = seatRepository.findByShowtimeId(showtime.getId());
        if (!existingSeats.isEmpty()) {
            log.debug("Los asientos ya fueron generados para la función {}", showtime.getId());
            return;
        }

        log.info("Generando 120 asientos para función {} ({} - {})",
                showtime.getId(),
                showtime.getMovie().getTitle(),
                showtime.getCinema().getName());

        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();
        List<Seat> seats = new ArrayList<>();
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

        for (String row : rows) {
            for (int number = 1; number <= 12; number++) {
                Seat seat = new Seat();
                seat.setShowtime(showtime);
                seat.setSeatNumber(row + number);
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setWeekId(currentWeek.getWeekId());
                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
        log.info("120 asientos generados para función {}", showtime.getId());

        occupyRandomSeats(showtime.getId());
    }

    private void occupyRandomSeats(Long showtimeId) {
        Random random = new Random();
        int seatsToOccupy = 20 + random.nextInt(11);

        log.info("🎲 Ocupando {} asientos aleatoriamente para función {}", seatsToOccupy, showtimeId);

        List<Seat> allSeats = seatRepository.findByShowtimeId(showtimeId);
        Collections.shuffle(allSeats);

        for (int i = 0; i < seatsToOccupy && i < allSeats.size(); i++) {
            Seat seat = allSeats.get(i);
            seat.setStatus(SeatStatus.RESERVED_RANDOM);
        }

        seatRepository.saveAll(allSeats.subList(0, Math.min(seatsToOccupy, allSeats.size())));

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Función no encontrada con ID: " + showtimeId));

        int availableSeats = 120 - seatsToOccupy;
        showtime.setAvailableSeats(availableSeats);
        showtimeRepository.save(showtime);

        log.info("{} asientos fueron ocupados, {} asientos disponibles", seatsToOccupy, availableSeats);
    }

    @Override
    @Transactional
    public void generateSeatsForAllShowtimes() {
        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();
        List<Showtime> showtimes = showtimeRepository.findByWeekId(currentWeek.getWeekId());

        if (showtimes.isEmpty()) {
            log.warn("No hay funciones en la semana actual. No se pueden generar asientos.");
            return;
        }

        log.info("Generando asientos para {} funciones de la semana actual", showtimes.size());

        int generatedCount = 0;
        for (Showtime showtime : showtimes) {
            generateSeatsForShowtime(showtime);
            generatedCount++;
        }

        log.info("Se generaron asientos para {} funciones", generatedCount);
    }

    @Override
    @Transactional
    public void reserveSeats(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un asiento");
        }

        log.info("Reservando {} asientos: {}", seatIds.size(), seatIds);

        List<Seat> seats = seatRepository.findAllById(seatIds);

        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Algunos asientos no fueron encontrados");
        }

        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("El asiento " + seat.getSeatNumber() + " no está disponible");
            }
        }

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.RESERVED_USER);
        }

        seatRepository.saveAll(seats);

        Showtime showtime = seats.get(0).getShowtime();
        int currentAvailable = showtime.getAvailableSeats();
        showtime.setAvailableSeats(currentAvailable - seats.size());
        showtimeRepository.save(showtime);

        log.info("Asientos reservados exitosamente");
    }

    @Override
    public SeatDTO convertToDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setSeatNumber(seat.getSeatNumber());
        dto.setStatus(seat.getStatus());
        return dto;
    }
}