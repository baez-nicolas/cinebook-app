package com.cinebook.backend.services.interfaces;

import com.cinebook.backend.dtos.BookingRequestDTO;
import com.cinebook.backend.dtos.BookingResponseDTO;
import com.cinebook.backend.entities.Booking;

import java.util.List;

public interface IBookingService {
    BookingResponseDTO createBooking(String userEmail, BookingRequestDTO request);
    List<BookingResponseDTO> getBookingsByUser(String userEmail);
    BookingResponseDTO getBookingByConfirmationCode(String confirmationCode);
    BookingResponseDTO getBookingById(Long bookingId);
    List<BookingResponseDTO> getAllBookings();
    String generateConfirmationCode();
    BookingResponseDTO convertToDTO(Booking booking);
}

