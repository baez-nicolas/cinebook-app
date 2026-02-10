package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.BookingRequestDTO;
import com.cinebook.backend.dtos.BookingResponseDTO;
import com.cinebook.backend.entities.Booking;

import java.util.List;

public interface IBookingService {
    BookingResponseDTO createBooking(String userName, BookingRequestDTO request);
    List<BookingResponseDTO> getBookingsByUser(String userName);
    BookingResponseDTO getBookingByConfirmationCode(String confirmationCode);
    BookingResponseDTO getBookingById(Long bookingId);
    List<BookingResponseDTO> getAllBookings();
    String generateConfirmationCode();
    BookingResponseDTO convertToDTO(Booking booking);
}