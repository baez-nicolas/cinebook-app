package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByConfirmationCode(String confirmationCode);

    List<Booking> findByShowtimeIdIn(List<Long> showtimeIds);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.showtime.id IN :showtimeIds")
    int deleteByShowtimeIdIn(@Param("showtimeIds") List<Long> showtimeIds);

    @Query("""
        SELECT b FROM Booking b 
        JOIN b.user u 
        JOIN b.showtime s 
        JOIN s.movie m 
        JOIN s.cinema c 
        WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        """)
    List<Booking> searchBookings(@Param("searchTerm") String searchTerm);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.weekId = :weekId")
    void deleteByWeekId(@Param("weekId") Long weekId);
}
