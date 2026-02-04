package com.cinebook.backend.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {

    @NotNull(message = "Showtime ID es obligatorio")
    private Long showtimeId;

    @NotEmpty(message = "Debe seleccionar al menos un asiento")
    private List<Long> seatIds;
}