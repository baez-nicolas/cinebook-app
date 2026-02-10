package com.cinebook.backend.repositories;
import com.cinebook.backend.entities.Booking;
import com.cinebook.backend.entities.enums.PaymentStatus;
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
    List<Booking> findByShowtimeId(Long showtimeId);
    List<Booking> findByUserIdAndPaymentStatus(Long userId, PaymentStatus status);
    List<Booking> findByWeekId(Long weekId);
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.weekId = :weekId")
    void deleteByWeekId(@Param("weekId") Long weekId);
}
