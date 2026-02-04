package com.cinebook.backend.services.impl;

import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.repositories.WeeklyScheduleRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyScheduleServiceImpl implements IWeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    @Override
    public WeeklySchedule getCurrentWeek() {
        return weeklyScheduleRepository.findByIsActiveTrue()
                .orElseGet(() -> {
                    log.info("No hay semana activa, creando una nueva...");
                    return createNewWeek(getCurrentThursday());
                });
    }

    @Override
    @Transactional
    public boolean checkAndResetIfNeeded() {
        LocalDate today = LocalDate.now();
        LocalDate currentThursday = getCurrentThursday();

        WeeklySchedule currentWeek = weeklyScheduleRepository.findByIsActiveTrue()
                .orElse(null);

        if (currentWeek == null) {
            log.info("No existe semana activa. Creando nueva semana...");
            createNewWeek(currentThursday);
            return true;
        }

        if (today.isAfter(currentWeek.getWeekEndDate())) {
            log.info("🔄 RESET SEMANAL: Pasó el miércoles, iniciando nueva semana...");
            performWeeklyReset(currentWeek, currentThursday);
            return true;
        }

        log.debug("No es necesario reset. Semana actual: {} - {}",
                currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());
        return false;
    }

    @Transactional
    protected void performWeeklyReset(WeeklySchedule oldWeek, LocalDate newThursday) {
        log.info("🗑️ Eliminando datos de la semana anterior (weekId: {})...", oldWeek.getWeekId());

        bookingRepository.deleteByWeekId(oldWeek.getWeekId());
        log.info("✅ Reservas eliminadas");

        seatRepository.deleteByWeekId(oldWeek.getWeekId());
        log.info("✅ Asientos eliminados");

        showtimeRepository.deleteByWeekId(oldWeek.getWeekId());
        log.info("✅ Funciones eliminadas");

        oldWeek.setIsActive(false);
        weeklyScheduleRepository.save(oldWeek);
        log.info("✅ Semana anterior desactivada");

        WeeklySchedule newWeek = createNewWeek(newThursday);
        log.info("🎉 Nueva semana creada: {} (weekId: {})",
                newWeek.getWeekStartDate(), newWeek.getWeekId());
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

        WeeklySchedule newWeek = new WeeklySchedule();
        newWeek.setWeekId(weekId);
        newWeek.setWeekStartDate(weekStart);
        newWeek.setWeekEndDate(weekEnd);
        newWeek.setIsActive(true);
        newWeek.setCreatedAt(LocalDate.now());

        WeeklySchedule saved = weeklyScheduleRepository.save(newWeek);
        log.info("📅 Nueva semana creada: {} a {} (weekId: {})",
                weekStart, weekEnd, weekId);

        return saved;
    }

    @Override
    public Long calculateWeekId(LocalDate date) {
        int year = date.getYear();
        int weekNumber = date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        return Long.valueOf(String.format("%d%02d", year, weekNumber));
    }

    @Override
    public LocalDate getNextThursday(LocalDate fromDate) {
        return fromDate.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
    }

    @Override
    public LocalDate getCurrentThursday() {
        LocalDate today = LocalDate.now();

        if (today.getDayOfWeek() == DayOfWeek.THURSDAY) {
            return today;
        }

        LocalDate thursday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY));

        if (thursday.isAfter(today)) {
            thursday = today.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
        }

        return thursday;
    }
}