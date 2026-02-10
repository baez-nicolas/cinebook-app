package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowtimeId(Long showtimeId);
    List<Seat> findByShowtimeIdAndStatus(Long showtimeId, SeatStatus status);
    List<Seat> findByWeekId(Long weekId);

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.weekId = :weekId")
    void deleteByWeekId(@Param("weekId") Long weekId);
}