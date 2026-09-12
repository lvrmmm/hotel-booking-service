package ru.lvrmmm.hotelbookingservice.booking.dto.response;

import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse (

        UUID id,
        UUID userId,
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        BookingStatus bookingStatus,
        BigDecimal totalPrice,
        LocalDateTime createdAt
){
    public static BookingResponse from(Booking booking){
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getRoom().getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                booking.getBookingStatus(),
                booking.getTotalPrice(),
                booking.getCreatedAt()
        );
    }
}
