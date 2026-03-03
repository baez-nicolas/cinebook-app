package com.cinebook.backend.dtos;

import com.cinebook.backend.entities.enums.PaymentStatus;
import com.cinebook.backend.entities.enums.ShowtimeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Long bookingId;
    private String confirmationCode;
    private String userName;

    private String movieTitle;
    private String moviePosterUrl;

    private String cinemaName;
    private String cinemaAddress;

    private LocalDateTime showDateTime;
    private ShowtimeType showtimeType;

    private List<String> seatNumbers;

    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;
    private LocalDateTime bookingDateTime;
}