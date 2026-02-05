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

    private final Random random = new Random();

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
    public void generateSeatsForShowtime(Long showtimeId) {
        log.info("💺 Generando asientos para la función ID: {}", showtimeId);

        List<Seat> existingSeats = seatRepository.findByShowtimeId(showtimeId);
        if (!existingSeats.isEmpty()) {
            log.info("⚠️ Ya existen {} asientos para esta función. Saltando generación.", existingSeats.size());
            return;
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Función no encontrada con ID: " + showtimeId));

        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();

        List<Seat> seats = new ArrayList<>();
        char[] rows = {'A', 'B', 'C', 'D', 'E'};

        for (char row : rows) {
            for (int col = 1; col <= 6; col++) {
                Seat seat = new Seat();
                seat.setShowtime(showtime);
                seat.setSeatNumber(row + String.valueOf(col));
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setWeekId(currentWeek.getWeekId());
                seats.add(seat);
            }
        }

        int randomOccupied = 7 + random.nextInt(9);
        log.info("Marcando {} asientos como ocupados (aleatorio)", randomOccupied);

        Collections.shuffle(seats);
        for (int i = 0; i < randomOccupied; i++) {
            seats.get(i).setStatus(SeatStatus.RESERVED_RANDOM);
        }

        seatRepository.saveAll(seats);

        int availableSeats = 30 - randomOccupied;
        showtime.setAvailableSeats(availableSeats);
        showtimeRepository.save(showtime);

        log.info("✅ Generados 30 asientos: {} disponibles, {} ocupados",
                availableSeats, randomOccupied);
    }

    @Override
    @Transactional
    public void generateSeatsForAllShowtimes() {
        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();
        List<Showtime> showtimes = showtimeRepository.findByWeekId(currentWeek.getWeekId());

        if (showtimes.isEmpty()) {
            log.warn("⚠️ No hay funciones en la semana actual. No se pueden generar asientos.");
            return;
        }

        log.info("💺 Generando asientos para {} funciones de la semana actual", showtimes.size());

        int generatedCount = 0;
        for (Showtime showtime : showtimes) {
            generateSeatsForShowtime(showtime.getId());
            generatedCount++;
        }

        log.info("✅ Se generaron asientos para {} funciones", generatedCount);
    }

    @Override
    @Transactional
    public void reserveSeats(List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Debe seleccionar al menos un asiento");
        }

        log.info("🎫 Reservando {} asientos: {}", seatIds.size(), seatIds);

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

        log.info("✅ Asientos reservados exitosamente");
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