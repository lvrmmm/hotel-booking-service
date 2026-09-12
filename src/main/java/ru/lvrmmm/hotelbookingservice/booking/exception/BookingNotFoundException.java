package ru.lvrmmm.hotelbookingservice.booking.exception;

import ru.lvrmmm.hotelbookingservice.common.exception.NotFoundException;

import java.util.UUID;

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(UUID id) {
        super("Booking with id = " + id + " not found");
    }
}
