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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyScheduleServiceImpl implements IWeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final IShowtimeService showtimeService;

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
            log.warn("⚠️ Hay {} semanas activas, limpiando y dejando solo una...", activeWeeks.size());
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
        log.info("⏰ TAREA PROGRAMADA: Medianoche - Actualizando ventana de funciones...");
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
            log.warn("⚠️ Limpiando {} semanas activas duplicadas...", activeWeeks.size());
            for (int i = 1; i < activeWeeks.size(); i++) {
                activeWeeks.get(i).setIsActive(false);
            }
            weeklyScheduleRepository.saveAll(activeWeeks);
        }

        WeeklySchedule currentWeek = activeWeeks.get(0);

        if (!currentWeek.getWeekStartDate().equals(today)) {
            log.info("🔄 ACTUALIZACIÓN DIARIA: Moviendo ventana de funciones...");
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
        log.info("║  🔄 ACTUALIZACIÓN DIARIA DE FUNCIONES                   ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📅 Fecha actual: {}", today);

        LocalDate oldStartDate = currentWeek.getWeekStartDate();
        LocalDate newEndDate = today.plusDays(7);
        Long newWeekId = calculateWeekId(today);

        log.info("🗑️ [1/4] Eliminando funciones y reservas del día pasado ({})...", oldStartDate);

        List<Showtime> oldShowtimes = showtimeRepository.findByWeekId(currentWeek.getWeekId()).stream()
                .filter(st -> st.getShowDateTime().toLocalDate().isBefore(today))
                .toList();

        if (!oldShowtimes.isEmpty()) {
            List<Long> oldShowtimeIds = oldShowtimes.stream().map(Showtime::getId).toList();

            bookingRepository.deleteByShowtimeIdIn(oldShowtimeIds);
            log.info("   ✅ Reservas del día pasado eliminadas");

            List<Seat> oldSeats = seatRepository.findAll().stream()
                    .filter(seat -> oldShowtimeIds.contains(seat.getShowtime().getId()))
                    .toList();
            seatRepository.deleteAll(oldSeats);
            log.info("   ✅ Asientos del día pasado eliminados");

            showtimeRepository.deleteAll(oldShowtimes);
            log.info("   ✅ {} funciones del día pasado eliminadas", oldShowtimes.size());
        }

        log.info("📅 [2/4] Actualizando weekId de funciones y asientos restantes...");
        List<Showtime> remainingShowtimes = showtimeRepository.findByWeekId(currentWeek.getWeekId());
        remainingShowtimes.forEach(st -> st.setWeekId(newWeekId));
        showtimeRepository.saveAll(remainingShowtimes);

        List<Seat> remainingSeats = seatRepository.findByWeekId(currentWeek.getWeekId());
        remainingSeats.forEach(seat -> seat.setWeekId(newWeekId));
        seatRepository.saveAll(remainingSeats);
        log.info("   ✅ WeekId actualizado para {} funciones", remainingShowtimes.size());

        log.info("🔧 [3/4] Actualizando ventana de fechas...");
        currentWeek.setWeekStartDate(today);
        currentWeek.setWeekEndDate(newEndDate);
        currentWeek.setWeekId(newWeekId);
        weeklyScheduleRepository.save(currentWeek);

        log.info("🎬 [4/4] Generando funciones para el nuevo día 7 ({})...", newEndDate);
        showtimeService.generateShowtimesForDate(newEndDate);

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  ✅ ACTUALIZACIÓN COMPLETADA                            ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📊 Nueva ventana: {} - {}", today, newEndDate);
        log.info("🎬 Funciones actualizadas automáticamente. Siempre hay 7 días disponibles.");
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
        log.info("✅ Nueva ventana de 7 días creada: {} - {} (weekId: {})", startDate, weekEnd, weekId);

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