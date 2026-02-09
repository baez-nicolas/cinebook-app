package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.SeatDTO;
import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.Showtime;

import java.util.List;

public interface ISeatService {
    List<SeatDTO> getSeatsByShowtime(Long showtimeId);
    List<SeatDTO> getAvailableSeatsByShowtime(Long showtimeId);
    void generateSeatsForShowtime(Showtime showtime);
    void generateSeatsForAllShowtimes();
    void reserveSeats(List<Long> seatIds);
    SeatDTO convertToDTO(Seat seat);
}