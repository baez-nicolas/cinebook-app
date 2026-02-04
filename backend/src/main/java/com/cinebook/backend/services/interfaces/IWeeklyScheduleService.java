package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.entities.WeeklySchedule;

import java.time.LocalDate;

public interface IWeeklyScheduleService {
    WeeklySchedule getCurrentWeek();
    boolean checkAndResetIfNeeded();
    WeeklySchedule createNewWeek(LocalDate thursdayDate);
    Long calculateWeekId(LocalDate date);
    LocalDate getNextThursday(LocalDate fromDate);
    LocalDate getCurrentThursday();
}