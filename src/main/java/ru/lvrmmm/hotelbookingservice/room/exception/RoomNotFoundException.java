package ru.lvrmmm.hotelbookingservice.room.exception;

import ru.lvrmmm.hotelbookingservice.common.exception.NotFoundException;

public class RoomNotFoundException extends NotFoundException {
    public RoomNotFoundException(Long id) {
        super("Room with id = " + id + " not found!");
    }
}
