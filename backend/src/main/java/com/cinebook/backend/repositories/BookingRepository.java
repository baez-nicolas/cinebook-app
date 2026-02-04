package com.cinebook.backend.repositories;

import com.cinebook.backend.entities.Booking;
import com.cinebook.backend.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserName(String userName);
    Optional<Booking> findByConfirmationCode(String confirmationCode);
    List<Booking> findByShowtimeId(Long showtimeId);
    List<Booking> findByUserNameAndPaymentStatus(String userName, PaymentStatus status);
    List<Booking> findByWeekId(Long weekId);
    void deleteByWeekId(Long weekId);
}