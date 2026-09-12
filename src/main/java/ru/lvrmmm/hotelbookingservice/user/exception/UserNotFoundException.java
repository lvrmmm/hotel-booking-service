package ru.lvrmmm.hotelbookingservice.user.exception;

import ru.lvrmmm.hotelbookingservice.common.exception.NotFoundException;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(UUID id) {
        super("User with id = " + id + " not found");
    }
}
