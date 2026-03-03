package com.cinebook.backend.dtos;

import com.cinebook.backend.entities.enums.ShowtimeType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Long cinemaId;
    private String cinemaName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "America/Argentina/Buenos_Aires")
    private LocalDateTime showDateTime;
    private ShowtimeType type;
    private BigDecimal price;
    private Integer availableSeats;
    private Integer totalSeats;
}