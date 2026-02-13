package com.cinebook.backend.services.impl;

import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.entities.enums.SeatStatus;
import com.cinebook.backend.repositories.WeeklyScheduleRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public WeeklySchedule getCurrentWeek() {
        List<WeeklySchedule> activeWeeks = weeklyScheduleRepository.findAll().stream()
                .filter(WeeklySchedule::getIsActive)
                .toList();

        if (activeWeeks.isEmpty()) {
            log.info("No hay semana activa, creando una nueva...");
            return createNewWeek(getCurrentThursday());
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

    @Scheduled(cron = "0 0 23 * * WED", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void scheduledWeeklyReset() {
        log.info("⏰ TAREA PROGRAMADA: Miércoles 23:00 - Iniciando reset semanal automático...");
        checkAndResetIfNeeded();
    }

    @Override
    @Transactional
    public boolean checkAndResetIfNeeded() {
        LocalDate today = LocalDate.now();
        LocalDate currentThursday = getCurrentThursday();

        List<WeeklySchedule> activeWeeks = weeklyScheduleRepository.findAll().stream()
                .filter(WeeklySchedule::getIsActive)
                .toList();

        if (activeWeeks.isEmpty()) {
            log.info("No existe semana activa. Creando nueva semana...");
            createNewWeek(currentThursday);
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

        if (today.isAfter(currentWeek.getWeekEndDate())) {
            log.info("🔄 RESET SEMANAL: Pasó el miércoles, iniciando reset...");
            performWeeklyReset(currentWeek, currentThursday);
            return true;
        }

        log.debug("No es necesario reset. Semana actual: {} - {}",
                currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());
        return false;
    }

    @Transactional
    protected void performWeeklyReset(WeeklySchedule oldWeek, LocalDate newThursday) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  🔄 RESET SEMANAL AUTOMÁTICO - MIÉRCOLES 23:00         ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📅 Semana anterior: {} (weekId: {})", oldWeek.getWeekStartDate(), oldWeek.getWeekId());

        Long newWeekId = calculateWeekId(newThursday);

        log.info("🗑️ [1/4] Eliminando reservas de la semana anterior...");
        bookingRepository.deleteByWeekId(oldWeek.getWeekId());
        log.info("   ✅ Reservas eliminadas");

        log.info("🔄 [2/4] Reseteando asientos a AVAILABLE...");
        List<Seat> allSeats = seatRepository.findByWeekId(oldWeek.getWeekId());
        int resettedSeats = 0;
        for (Seat seat : allSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setWeekId(newWeekId);
            resettedSeats++;
        }
        seatRepository.saveAll(allSeats);
        log.info("   ✅ {} asientos reseteados", resettedSeats);

        log.info("📅 [3/4] Actualizando fechas de funciones (+7 días)...");
        List<Showtime> showtimes = showtimeRepository.findByWeekId(oldWeek.getWeekId());
        int updatedShowtimes = 0;
        for (Showtime showtime : showtimes) {
            LocalDateTime oldDateTime = showtime.getShowDateTime();
            LocalDateTime newDateTime = oldDateTime.plusWeeks(1);
            showtime.setShowDateTime(newDateTime);
            showtime.setWeekId(newWeekId);
            updatedShowtimes++;

            if (updatedShowtimes <= 3) {
                log.info("   📽️ {} {} → {}",
                        showtime.getMovie().getTitle(),
                        oldDateTime,
                        newDateTime);
            }
        }
        showtimeRepository.saveAll(showtimes);
        log.info("   ✅ {} funciones actualizadas", updatedShowtimes);

        log.info("🔧 [4/4] Actualizando control de semanas...");
        oldWeek.setIsActive(false);
        weeklyScheduleRepository.save(oldWeek);

        WeeklySchedule newWeek = createNewWeek(newThursday);

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  ✅ RESET COMPLETADO EXITOSAMENTE                       ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("📊 Resumen:");
        log.info("   • Asientos reseteados: {}", resettedSeats);
        log.info("   • Funciones actualizadas: {}", updatedShowtimes);
        log.info("   • Nueva semana: {} - {} (weekId: {})",
                newWeek.getWeekStartDate(), newWeek.getWeekEndDate(), newWeek.getWeekId());
        log.info("🎬 Todas las funciones están disponibles de nuevo");
    }

    @Override
    @Transactional
    public WeeklySchedule createNewWeek(LocalDate thursdayDate) {
        if (thursdayDate.getDayOfWeek() != DayOfWeek.THURSDAY) {
            thursdayDate = thursdayDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY));
        }

        LocalDate weekStart = thursdayDate;
        LocalDate weekEnd = thursdayDate.plusDays(6);
        Long weekId = calculateWeekId(thursdayDate);

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
        newWeek.setWeekStartDate(weekStart);
        newWeek.setWeekEndDate(weekEnd);
        newWeek.setIsActive(true);
        newWeek.setCreatedAt(LocalDate.now());

        weeklyScheduleRepository.save(newWeek);
        log.info("✅ Nueva semana creada: {} - {} (weekId: {})", weekStart, weekEnd, weekId);

        return newWeek;
    }

    @Override
    public Long calculateWeekId(LocalDate date) {
        long epochDay = date.toEpochDay();
        long weekNumber = epochDay / 7;
        return weekNumber;
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