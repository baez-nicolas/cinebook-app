package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByWeekId(Long weekId);
    List<Showtime> findByMovieId(Long movieId);
    List<Showtime> findByCinemaId(Long cinemaId);
    List<Showtime> findByCinemaIdAndMovieId(Long cinemaId, Long movieId);
    List<Showtime> findByShowDateTimeBetween(LocalDateTime start, LocalDateTime end);
    void deleteByWeekId(Long weekId);
}