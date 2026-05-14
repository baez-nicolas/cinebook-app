package com.cinebook.backend.services.impl;

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
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

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
    public void scheduledDailyUpdate() {
        log.info("TAREA PROGRAMADA: Medianoche - Actualizando ventana de funciones...");
        checkAndResetIfNeeded();
    }

    @Override
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
            WeeklySchedule first = activeWeeks.get(0);
            for (int i = 1; i < activeWeeks.size(); i++) {
                activeWeeks.get(i).setIsActive(false);
            }
            weeklyScheduleRepository.saveAll(activeWeeks);
        }

        WeeklySchedule currentWeek = activeWeeks.get(0);

        if (!currentWeek.getWeekStartDate().equals(today)) {
            long daysDifference = ChronoUnit.DAYS.between(currentWeek.getWeekStartDate(), today);

            if (daysDifference > 7) {
                log.warn("REINICIO COMPLETO: Han pasado {} días desde la última actualización", daysDifference);
                log.warn("Recreando toda la ventana de 7 días desde cero...");
                performCompleteReset(currentWeek.getId(), today);
            } else {
                log.info("ACTUALIZACIÓN DIARIA: Moviendo ventana de funciones...");
                performDailyUpdate(currentWeek.getId(), currentWeek.getWeekStartDate(), today);
            }
            return true;
        }

        // La ventana ya empieza hoy, pero pueden faltar días intermedios.
        // generateShowtimesForDate es idempotente: si ya existen funciones para ese día, no hace nada.
        log.info("Ventana ya actualizada ({} - {}). Verificando gaps...",
                currentWeek.getWeekStartDate(), currentWeek.getWeekEndDate());
        for (int i = 0; i < 7; i++) {
            LocalDate dateToCheck = today.plusDays(i);
            try {
                showtimeService.generateShowtimesForDate(dateToCheck);
            } catch (Exception e) {
                log.error("Error al verificar gap para {}: {}", dateToCheck, e.getMessage(), e);
            }
        }
        return false;
    }

    protected void performDailyUpdate(Long weekId, LocalDate dateToRemove, LocalDate today) {
        log.info("ACTUALIZACIÓN DIARIA: {} -> {}", dateToRemove, today);

        // Eliminar funciones del día más antiguo que ya no entra en la ventana
        List<Showtime> showtimesToRemove = showtimeRepository.findAll().stream()
                .filter(s -> s.getShowDateTime().toLocalDate().equals(dateToRemove))
                .toList();

        if (!showtimesToRemove.isEmpty()) {
            List<Long> ids = showtimesToRemove.stream().map(Showtime::getId).collect(Collectors.toList());
            log.info("Eliminando {} funciones del día {} y sus asientos...", ids.size(), dateToRemove);
            seatRepository.deleteBookingSeatsByShowtimeIds(ids);
            seatRepository.deleteByShowtimeIds(ids);
            showtimeRepository.deleteAllByIdInBatch(ids);
            log.info("Funciones del día {} eliminadas", dateToRemove);
        }

        updateWeeklySchedule(weekId, today, today.plusDays(6));

        // Generar TODOS los días de la ventana que falten (no sólo el último).
        // generateShowtimesForDate ya verifica duplicados, es seguro llamarlo para cada día.
        log.info("Verificando y generando funciones para los 7 días de la ventana: {} - {}", today, today.plusDays(6));
        for (int i = 0; i < 7; i++) {
            LocalDate dateToGenerate = today.plusDays(i);
            try {
                log.info("Verificando día {} de 7: {}", i + 1, dateToGenerate);
                showtimeService.generateShowtimesForDate(dateToGenerate);
            } catch (Exception e) {
                log.error("Error al generar funciones para {}: {}. Continuando...", dateToGenerate, e.getMessage(), e);
            }
        }

        log.info("Actualización diaria completada exitosamente. Nueva ventana: {} - {}", today, today.plusDays(6));
    }

    protected void performCompleteReset(Long weekId, LocalDate today) {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║       REINICIO COMPLETO DE VENTANA DE FUNCIONES        ║");
        log.info("╠══════════════════════════════════════════════════════════╣");
        log.info("║  Fecha actual: {}", today);
        log.info("╚══════════════════════════════════════════════════════════╝");

        log.info("Paso 1: Eliminando junction table booking_seats...");
        seatRepository.deleteAllBookingSeatJunctions();

        log.info("Paso 2: Eliminando TODOS los asientos (bulk)...");
        seatRepository.deleteAllInBatch();
        log.info("Asientos eliminados");

        log.info("Paso 3: Eliminando TODAS las funciones (bulk)...");
        showtimeRepository.deleteAllInBatch();
        log.info("Funciones eliminadas");

        updateWeeklySchedule(weekId, today, today.plusDays(6));

        log.info("Paso 4: Generando funciones para los 7 días completos...");
        int diasGenerados = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate dateToGenerate = today.plusDays(i);
            log.info("Generando día {} de 7: {}", i + 1, dateToGenerate);
            try {
                showtimeService.generateShowtimesForDate(dateToGenerate);
                diasGenerados++;
            } catch (Exception e) {
                log.error("Error generando funciones para el día {}: {}. Continuando con el siguiente día...",
                        dateToGenerate, e.getMessage(), e);
            }
        }

        log.info("REINICIO COMPLETO finalizado. {} de 7 días generados. Nueva ventana: {} - {}",
                diasGenerados, today, today.plusDays(6));
    }

    @Transactional
    protected void updateWeeklySchedule(Long weekId, LocalDate startDate, LocalDate endDate) {
        weeklyScheduleRepository.findById(weekId).ifPresent(week -> {
            week.setWeekStartDate(startDate);
            week.setWeekEndDate(endDate);
            weeklyScheduleRepository.save(week);
            log.info("Ventana actualizada: {} - {}", startDate, endDate);
        });
    }

    @Override
    @Transactional
    public WeeklySchedule createNewWeek(LocalDate startDate) {
        LocalDate weekEnd = startDate.plusDays(6);
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

        for (int i = 0; i < 7; i++) {
            LocalDate dateToGenerate = startDate.plusDays(i);
            log.info("Generando funciones para el día: {}", dateToGenerate);
            try {
                showtimeService.generateShowtimesForDate(dateToGenerate);
            } catch (Exception e) {
                log.error("Error generando funciones para el día {}: {}. Continuando...",
                        dateToGenerate, e.getMessage(), e);
            }
        }
        log.info("Funciones generadas para toda la ventana de 7 días");

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

