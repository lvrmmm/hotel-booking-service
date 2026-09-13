package ru.lvrmmm.hotelbookingservice.user.exception;

public class RoleChangeNotAllowedException extends RuntimeException {
    public RoleChangeNotAllowedException(String message) {
        super(message);
    }
}