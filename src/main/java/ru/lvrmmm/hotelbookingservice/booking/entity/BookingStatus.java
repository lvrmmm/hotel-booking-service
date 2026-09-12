package ru.lvrmmm.hotelbookingservice.booking.entity;

public enum BookingStatus {
    PENDING,     // Ожидает подтверждения (например, ждём оплату)
    CONFIRMED,   // Подтверждено (оплачено, комната зарезервирована)
    CANCELLED,   // Отменено (пользователем или админом)
    COMPLETED    // Завершено (гость выехал)
}
