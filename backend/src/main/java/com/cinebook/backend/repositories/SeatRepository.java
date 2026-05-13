package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByShowtimeId(Long showtimeId);
    List<Seat> findByShowtimeIdAndStatus(Long showtimeId, SeatStatus status);
    List<Seat> findByWeekId(Long weekId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Seat s WHERE s.weekId = :weekId")
    void deleteByWeekId(@Param("weekId") Long weekId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM booking_seats", nativeQuery = true)
    void deleteAllBookingSeatJunctions();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM booking_seats WHERE seat_id IN (SELECT id FROM seats WHERE showtime_id IN :showtimeIds)", nativeQuery = true)
    void deleteBookingSeatsByShowtimeIds(@Param("showtimeIds") List<Long> showtimeIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM Seat s WHERE s.showtime.id IN :showtimeIds")
    void deleteByShowtimeIds(@Param("showtimeIds") List<Long> showtimeIds);
}