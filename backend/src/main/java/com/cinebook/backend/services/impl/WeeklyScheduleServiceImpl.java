package com.cinebook.backend.services.impl;

import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.repositories.WeeklyScheduleRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Slf4j
public class WeeklyScheduleServiceImpl implements IWeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final IShowtimeService showtimeService;

    public WeeklyScheduleServiceImpl(
            WeeklyScheduleRepository weeklyScheduleRepository,
            ShowtimeRepository showtimeRepository,
            SeatRepository seatRepository,
            BookingRepository bookingRepository,
            @Lazy IShowtimeService showtimeService
    ) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.showtimeService = showtimeService;
    }

    @Override
    @Transactional
    public WeeklySchedule getCurrentWeek() {
        List<WeeklySchedule> activeWeeks = weeklyScheduleRepository.findAll().stream()
                .filter(WeeklySchedule::getIsActive)
                .toList();

        if (activeWeeks.isEmpty()) {
            log.info("No hay semana activa, creando una nueva...");
            return createNewWeek(LocalDate.now());
        }

        if (activeWeeks.size() > 1) {
            log.warn("Hay {} semanas activas, limpiando y dejando solo una...", activeWeeks.size());
            WeeklySchedule keepWeek = activeWeeks.get(0);
            for (int i = 1; i < activeWeeks.size(); i++) {
                activeWeeks.get(i).setIsActive(false);
            }
            weeklyScheduleRepository.saveAll(activeWeeks);
            return keepWeek;
        }

        return activeWeeks.get(0);
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void scheduledDailyUpdate() {
        log.info("TAREA PROGRAMADA: Medianoche - Actualizando ventana de funciones...");
        checkAndResetIfNeeded();
    }

    @Override
    @Transactional
    public boolean checkAndResetIfNeeded() {
        LocalDate today = LocalDate.now();

        List<WeeklySchedule> activeWeeks = weeklyScheduleRepository.findAll().stream()
                .filter(WeeklySchedule::getIsActive)
                .toList();

        if (activeWeeks.isEmpty()) {
            log.info("No existe semana activa. Creando nueva semana...");
            createNewWeek(today);
            return true;
        }

        if (activeWeeks.size() > 1) {
            log.warn("Limpiando {} semanas activas duplicadas...", activeWeeks.size());
            for (int i = 1; i < activeWeeks.size(); i++) {
                activeWeeks.get(i).setIsActive(false);
            }
            weeklyScheduleRepository.saveAll(activeWeeks);
        }

        WeeklySchedule currentWeek = activeWeeks.get(0);

        if (!currentWeek.getWeekStartDate().equals(today)) {
            log.info("ACTUALIZACIÓN DIARIA: Moviendo ventana de funciones...");
            performDailyUpdate(currentWeek, today);
            return true;
        }

        log.debug("No es necesario actualizar. Ventana actual: {} - {}",
                currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());
        return false;
    }

    @Transactional
    protected void performDailyUpdate(WeeklySchedule currentWeek, LocalDate today) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  ACTUALIZACIÓN DIARIA DE VENTANA DE FUNCIONES          ║");
        log.info("╠══════════════════════════════════════════════════════════╣");
        log.info("║  Fecha actual: {}", today);
        log.info("║  Ventana actual: {} a {}", currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());
        log.info("╚══════════════════════════════════════════════════════════╝");

        LocalDate dateToRemove = currentWeek.getWeekStartDate();
        log.info("Eliminando funciones del día: {}", dateToRemove);

        List<Showtime> showtimesToRemove = showtimeRepository.findAll().stream()
                .filter(s -> s.getShowDateTime().toLocalDate().equals(dateToRemove))
                .toList();

        if (!showtimesToRemove.isEmpty()) {
            showtimeRepository.deleteAll(showtimesToRemove);
            log.info("Resultado: {} funciones eliminadas del día {}", showtimesToRemove.size(), dateToRemove);
            log.info("Nota: Los asientos asociados permanecen en la BD para mantener integridad referencial");
        }

        currentWeek.setWeekStartDate(today);
        currentWeek.setWeekEndDate(today.plusDays(6));
        weeklyScheduleRepository.save(currentWeek);

        log.info("Nueva ventana: {} a {}", currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());

        LocalDate newDate = today.plusDays(6);
        log.info("Generando funciones para el nuevo día: {}", newDate);

        showtimeService.generateShowtimesForDate(newDate);

        log.info("Actualización diaria completada exitosamente");
        log.info("═══════════════════════════════════════════════════════════");
    }

    @Override
    @Transactional
    public WeeklySchedule createNewWeek(LocalDate startDate) {
        LocalDate weekEnd = startDate.plusDays(7);
        Long weekId = calculateWeekId(startDate);

        List<WeeklySchedule> activeWeeks = weeklyScheduleRepository.findAll().stream()
                .filter(WeeklySchedule::getIsActive)
                .toList();

        if (!activeWeeks.isEmpty()) {
            log.info("Desactivando {} semanas activas existentes", activeWeeks.size());
            activeWeeks.forEach(week -> week.setIsActive(false));
            weeklyScheduleRepository.saveAll(activeWeeks);
        }

        WeeklySchedule newWeek = new WeeklySchedule();
        newWeek.setWeekId(weekId);
        newWeek.setWeekStartDate(startDate);
        newWeek.setWeekEndDate(weekEnd);
        newWeek.setIsActive(true);
        newWeek.setCreatedAt(LocalDate.now());

        weeklyScheduleRepository.save(newWeek);
        log.info("Nueva ventana de 7 días creada: {} - {} (weekId: {})", startDate, weekEnd, weekId);

        return newWeek;
    }

    @Override
    public Long calculateWeekId(LocalDate date) {
        long epochDay = date.toEpochDay();
        return epochDay / 7;
    }

    @Override
    public LocalDate getNextThursday(LocalDate fromDate) {
        return fromDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
    }

    @Override
    public LocalDate getCurrentThursday() {
        LocalDate today = LocalDate.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        if (currentDay == DayOfWeek.THURSDAY) {
            return today;
        } else if (currentDay.getValue() < DayOfWeek.THURSDAY.getValue()) {
            return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY));
        } else {
            return today.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        }
    }
}