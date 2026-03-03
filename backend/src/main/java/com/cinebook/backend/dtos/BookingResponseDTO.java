package com.cinebook.backend.dtos;

import com.cinebook.backend.entities.enums.PaymentStatus;
import com.cinebook.backend.entities.enums.ShowtimeType;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    // Datos de la película
    private String movieTitle;
    private String moviePosterUrl;

    // Datos del cine
    private String cinemaName;
    private String cinemaAddress;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "America/Argentina/Buenos_Aires")
    private LocalDateTime showDateTime;
    private ShowtimeType showtimeType;

    private List<String> seatNumbers;

    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "America/Argentina/Buenos_Aires")
    private LocalDateTime bookingDateTime;
}