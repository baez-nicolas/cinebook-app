package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.WeeklySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {
    Optional<WeeklySchedule> findByIsActiveTrue();
    Optional<WeeklySchedule> findByWeekId(Long weekId);
    Optional<WeeklySchedule> findByWeekStartDateAndWeekEndDate(LocalDate weekStartDate, LocalDate weekEndDate);
}