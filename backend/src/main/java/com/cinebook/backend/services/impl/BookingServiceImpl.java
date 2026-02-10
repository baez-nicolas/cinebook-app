package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.BookingRequestDTO;
import com.cinebook.backend.dtos.BookingResponseDTO;
import com.cinebook.backend.entities.Booking;
import com.cinebook.backend.entities.Seat;
import com.cinebook.backend.entities.Showtime;
import com.cinebook.backend.entities.WeeklySchedule;
import com.cinebook.backend.entities.enums.PaymentStatus;
import com.cinebook.backend.entities.enums.SeatStatus;
import com.cinebook.backend.repositories.BookingRepository;
import com.cinebook.backend.repositories.SeatRepository;
import com.cinebook.backend.repositories.ShowtimeRepository;
import com.cinebook.backend.services.interfaces.IBookingService;
import com.cinebook.backend.services.interfaces.ISeatService;
import com.cinebook.backend.services.interfaces.IWeeklyScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final ISeatService seatService;
    private final IWeeklyScheduleService weeklyScheduleService;

    @Override
    @Transactional
    public BookingResponseDTO createBooking(String userName, BookingRequestDTO request) {
        log.info("🎫 Procesando reserva para usuario: {}", userName);

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("Función no encontrada con ID: " + request.getShowtimeId()));

        if (showtime.getShowDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se pueden reservar funciones pasadas");
        }

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new RuntimeException("Algunos asientos no fueron encontrados");
        }

        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("El asiento " + seat.getSeatNumber() + " no está disponible");
            }
        }

        BigDecimal pricePerSeat = showtime.getPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(seats.size()));

        log.info("💰 Precio total: ${} ({} asientos × ${})", totalPrice, seats.size(), pricePerSeat);

        seatService.reserveSeats(request.getSeatIds());

        WeeklySchedule currentWeek = weeklyScheduleService.getCurrentWeek();

        Booking booking = new Booking();
        booking.setUserName(userName);
        booking.setShowtime(showtime);
        booking.setSeats(seats);
        booking.setTotalPrice(totalPrice);
        booking.setPaymentStatus(PaymentStatus.CONFIRMED);
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setWeekId(currentWeek.getWeekId());
        booking.setConfirmationCode(generateConfirmationCode());

        Booking savedBooking = bookingRepository.save(booking);

        log.info("✅ Reserva creada: ID {} - Código: {}", savedBooking.getId(), savedBooking.getConfirmationCode());

        return convertToDTO(savedBooking);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByUser(String userName) {
        log.info("Obteniendo reservas del usuario: {}", userName);
        return bookingRepository.findByUserName(userName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        log.info("Obteniendo todas las reservas");
        return bookingRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO getBookingByConfirmationCode(String confirmationCode) {
        log.info("Buscando reserva con código: {}", confirmationCode);
        Booking booking = bookingRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con código: " + confirmationCode));
        return convertToDTO(booking);
    }

    @Override
    public BookingResponseDTO getBookingById(Long bookingId) {
        log.info("Obteniendo reserva con ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + bookingId));
        return convertToDTO(booking);
    }

    @Override
    public String generateConfirmationCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = bookingRepository.count() + 1;
        String sequence = String.format("%04d", count);
        return String.format("CNB-%s-%s", date, sequence);
    }

    @Override
    public BookingResponseDTO convertToDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();

        dto.setBookingId(booking.getId());
        dto.setConfirmationCode(booking.getConfirmationCode());
        dto.setUserName(booking.getUserName());

        dto.setMovieTitle(booking.getShowtime().getMovie().getTitle());
        dto.setMoviePosterUrl(booking.getShowtime().getMovie().getPosterUrl());

        dto.setCinemaName(booking.getShowtime().getCinema().getName());
        dto.setCinemaAddress(booking.getShowtime().getCinema().getAddress());

        dto.setShowDateTime(booking.getShowtime().getShowDateTime());
        dto.setShowtimeType(booking.getShowtime().getType());

        List<String> seatNumbers = booking.getSeats()
                .stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.toList());
        dto.setSeatNumbers(seatNumbers);

        dto.setTotalPrice(booking.getTotalPrice());
        dto.setPaymentStatus(booking.getPaymentStatus());
        dto.setBookingDateTime(booking.getBookingDateTime());

        return dto;
    }
}