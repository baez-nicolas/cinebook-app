package com.cinebook.backend.services.impl;

import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.repositories.WeeklyScheduleRepository;
import com.cinebook.backend.services.interfaces.IShowtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklyScheduleServiceImpl Tests")
class WeeklyScheduleServiceImplTest {

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private IShowtimeService showtimeService;

    private WeeklyScheduleServiceImpl weeklyScheduleService;

    private WeeklySchedule mockActiveWeek;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        weeklyScheduleService = new WeeklyScheduleServiceImpl(
                weeklyScheduleRepository,
                showtimeRepository,
                seatRepository,
                bookingRepository,
                showtimeService
        );

        today = LocalDate.now();

        mockActiveWeek = new WeeklySchedule();
        mockActiveWeek.setWeekId(1L);
        mockActiveWeek.setWeekStartDate(today);
        mockActiveWeek.setWeekEndDate(today.plusDays(6));
        mockActiveWeek.setIsActive(true);
        mockActiveWeek.setCreatedAt(today);
    }

    @Test
    @DisplayName("getCurrentWeek - Retorna semana activa existente")
    void getCurrentWeek_ReturnsActiveWeek() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockActiveWeek));

        WeeklySchedule result = weeklyScheduleService.getCurrentWeek();

        assertNotNull(result);
        assertEquals(1L, result.getWeekId());
        assertTrue(result.getIsActive());

        verify(weeklyScheduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getCurrentWeek - Crea nueva semana cuando no existe ninguna activa")
    void getCurrentWeek_CreatesNewWeek_WhenNoneExists() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WeeklySchedule result = weeklyScheduleService.getCurrentWeek();

        assertNotNull(result);
        assertTrue(result.getIsActive());

        verify(weeklyScheduleRepository, times(2)).findAll();
        verify(weeklyScheduleRepository, times(1)).save(any(WeeklySchedule.class));
    }

    @Test
    @DisplayName("getCurrentWeek - Limpia semanas duplicadas y retorna la primera")
    void getCurrentWeek_CleansDuplicates_ReturnsFirst() {
        WeeklySchedule week1 = new WeeklySchedule();
        week1.setWeekId(1L);
        week1.setIsActive(true);

        WeeklySchedule week2 = new WeeklySchedule();
        week2.setWeekId(2L);
        week2.setIsActive(true);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(week1, week2));
        when(weeklyScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        WeeklySchedule result = weeklyScheduleService.getCurrentWeek();

        assertNotNull(result);
        assertEquals(1L, result.getWeekId());
        assertFalse(week2.getIsActive());

        verify(weeklyScheduleRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("checkAndResetIfNeeded - Crea semana cuando no existe")
    void checkAndResetIfNeeded_CreatesWeek_WhenNoneExists() {
        when(weeklyScheduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = weeklyScheduleService.checkAndResetIfNeeded();

        assertTrue(result);

        verify(weeklyScheduleRepository, times(2)).findAll();
        verify(weeklyScheduleRepository, times(1)).save(any(WeeklySchedule.class));
    }

    @Test
    @DisplayName("checkAndResetIfNeeded - No actualiza si la fecha de inicio es hoy")
    void checkAndResetIfNeeded_DoesNotUpdate_WhenStartDateIsToday() {
        mockActiveWeek.setWeekStartDate(today);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockActiveWeek));

        boolean result = weeklyScheduleService.checkAndResetIfNeeded();

        assertFalse(result);

        verify(weeklyScheduleRepository, times(1)).findAll();
        verify(showtimeRepository, never()).findByWeekId(anyLong());
    }

    @Test
    @DisplayName("checkAndResetIfNeeded - Actualiza cuando la fecha de inicio no es hoy")
    void checkAndResetIfNeeded_Updates_WhenStartDateIsNotToday() {
        LocalDate yesterday = today.minusDays(1);
        mockActiveWeek.setWeekStartDate(yesterday);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(mockActiveWeek));
        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(mockActiveWeek);

        boolean result = weeklyScheduleService.checkAndResetIfNeeded();

        assertTrue(result);

        verify(weeklyScheduleRepository, times(1)).save(any(WeeklySchedule.class));
        verify(showtimeService, times(1)).generateShowtimesForDate(any(LocalDate.class));
    }

    @Test
    @DisplayName("createNewWeek - Crea nueva semana correctamente")
    void createNewWeek_CreatesCorrectly() {
        LocalDate startDate = LocalDate.now();

        when(weeklyScheduleRepository.findAll()).thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WeeklySchedule result = weeklyScheduleService.createNewWeek(startDate);

        assertNotNull(result);
        assertEquals(startDate, result.getWeekStartDate());
        assertEquals(startDate.plusDays(6), result.getWeekEndDate());
        assertTrue(result.getIsActive());

        verify(weeklyScheduleRepository, times(1)).save(any(WeeklySchedule.class));
    }

    @Test
    @DisplayName("createNewWeek - Desactiva semanas activas existentes")
    void createNewWeek_DeactivatesExistingActiveWeeks() {
        WeeklySchedule existingWeek = new WeeklySchedule();
        existingWeek.setWeekId(1L);
        existingWeek.setIsActive(true);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(existingWeek));
        when(weeklyScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WeeklySchedule result = weeklyScheduleService.createNewWeek(today);

        assertNotNull(result);
        assertFalse(existingWeek.getIsActive());

        verify(weeklyScheduleRepository, times(1)).saveAll(anyList());
        verify(weeklyScheduleRepository, times(1)).save(any(WeeklySchedule.class));
    }

    @Test
    @DisplayName("calculateWeekId - Calcula ID correctamente")
    void calculateWeekId_CalculatesCorrectly() {
        LocalDate testDate = LocalDate.of(2026, 2, 13);

        Long result = weeklyScheduleService.calculateWeekId(testDate);

        assertNotNull(result);
        assertTrue(result > 0);

        LocalDate anotherDate = testDate.plusWeeks(1);
        Long anotherResult = weeklyScheduleService.calculateWeekId(anotherDate);

        assertNotEquals(result, anotherResult);
    }

    @Test
    @DisplayName("calculateWeekId - Mismo ID para fechas en la misma semana")
    void calculateWeekId_SameIdForSameWeek() {
        LocalDate date1 = LocalDate.of(2026, 2, 13);
        LocalDate date2 = LocalDate.of(2026, 2, 14);

        Long id1 = weeklyScheduleService.calculateWeekId(date1);
        Long id2 = weeklyScheduleService.calculateWeekId(date2);

        assertEquals(id1, id2);
    }

    @Test
    @DisplayName("getNextThursday - Retorna el próximo jueves")
    void getNextThursday_ReturnsNextThursday() {
        LocalDate monday = LocalDate.of(2026, 2, 16);

        LocalDate result = weeklyScheduleService.getNextThursday(monday);

        assertEquals(DayOfWeek.THURSDAY, result.getDayOfWeek());
        assertTrue(result.isAfter(monday));
    }

    @Test
    @DisplayName("getNextThursday - Desde jueves retorna el siguiente jueves")
    void getNextThursday_FromThursday_ReturnsNextThursday() {
        LocalDate thursday = LocalDate.of(2026, 2, 12);
        assertEquals(DayOfWeek.THURSDAY, thursday.getDayOfWeek());

        LocalDate result = weeklyScheduleService.getNextThursday(thursday);

        assertEquals(DayOfWeek.THURSDAY, result.getDayOfWeek());
        assertTrue(result.isAfter(thursday));
        assertEquals(7, result.toEpochDay() - thursday.toEpochDay());
    }

    @Test
    @DisplayName("getCurrentThursday - Retorna hoy si es jueves")
    void getCurrentThursday_ReturnsToday_WhenTodayIsThursday() {
        LocalDate thursday = LocalDate.now();
        if (thursday.getDayOfWeek() != DayOfWeek.THURSDAY) {
            thursday = thursday.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        }

        LocalDate result = weeklyScheduleService.getCurrentThursday();

        assertEquals(DayOfWeek.THURSDAY, result.getDayOfWeek());
    }

    @Test
    @DisplayName("performDailyUpdate - Elimina funciones del día pasado")
    void performDailyUpdate_DeletesOldShowtimes() {
        LocalDate yesterday = today.minusDays(1);
        mockActiveWeek.setWeekStartDate(yesterday);

        Showtime oldShowtime = new Showtime();
        oldShowtime.setId(1L);
        oldShowtime.setShowDateTime(LocalDateTime.of(yesterday, java.time.LocalTime.of(19, 0)));

        when(showtimeRepository.findAll()).thenReturn(Arrays.asList(oldShowtime));
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(mockActiveWeek);

        weeklyScheduleService.performDailyUpdate(mockActiveWeek, today);

        verify(showtimeRepository, times(1)).deleteAll(anyList());
        verify(showtimeService, times(1)).generateShowtimesForDate(any(LocalDate.class));
        verify(seatRepository, never()).deleteAll(anyList());
    }

    @Test
    @DisplayName("performDailyUpdate - Actualiza ventana de fechas correctamente")
    void performDailyUpdate_UpdatesWeekDates() {
        LocalDate yesterday = today.minusDays(1);
        mockActiveWeek.setWeekStartDate(yesterday);

        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(mockActiveWeek);

        weeklyScheduleService.performDailyUpdate(mockActiveWeek, today);

        assertEquals(today, mockActiveWeek.getWeekStartDate());
        assertEquals(today.plusDays(6), mockActiveWeek.getWeekEndDate());

        verify(weeklyScheduleRepository, times(1)).save(any(WeeklySchedule.class));
    }

    @Test
    @DisplayName("performDailyUpdate - Genera funciones para el nuevo día 7")
    void performDailyUpdate_GeneratesShowtimesForNewDay() {
        LocalDate yesterday = today.minusDays(1);
        mockActiveWeek.setWeekStartDate(yesterday);
        LocalDate newEndDate = today.plusDays(6);

        when(showtimeRepository.findAll()).thenReturn(Collections.emptyList());
        when(weeklyScheduleRepository.save(any(WeeklySchedule.class))).thenReturn(mockActiveWeek);

        weeklyScheduleService.performDailyUpdate(mockActiveWeek, today);

        verify(showtimeService, times(1)).generateShowtimesForDate(newEndDate);
    }

    @Test
    @DisplayName("checkAndResetIfNeeded - Limpia semanas duplicadas")
    void checkAndResetIfNeeded_CleansDuplicateWeeks() {
        WeeklySchedule week1 = new WeeklySchedule();
        week1.setWeekId(1L);
        week1.setWeekStartDate(today);
        week1.setIsActive(true);

        WeeklySchedule week2 = new WeeklySchedule();
        week2.setWeekId(2L);
        week2.setWeekStartDate(today);
        week2.setIsActive(true);

        when(weeklyScheduleRepository.findAll()).thenReturn(Arrays.asList(week1, week2));
        when(weeklyScheduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = weeklyScheduleService.checkAndResetIfNeeded();

        assertFalse(result);
        assertFalse(week2.getIsActive());

        verify(weeklyScheduleRepository, times(1)).saveAll(anyList());
    }
}

