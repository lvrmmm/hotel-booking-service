package ru.lvrmmm.hotelbookingservice.booking.dto.response;

import java.time.LocalDate;

public record OccupiedRangeResponse(
        LocalDate checkIn,
        LocalDate checkOut
) {
}