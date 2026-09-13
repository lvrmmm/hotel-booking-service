package ru.lvrmmm.hotelbookingservice.user.exception;

public class AdminProtectionException extends RuntimeException {
    public AdminProtectionException(String message) {
        super(message);
    }
}
