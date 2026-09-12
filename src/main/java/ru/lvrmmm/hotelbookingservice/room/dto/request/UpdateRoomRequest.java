package ru.lvrmmm.hotelbookingservice.room.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;

import java.math.BigDecimal;

public record UpdateRoomRequest(

        @Min(value = 1, message = "Room number must be positive")
        Integer roomNumber,

        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal pricePerNight,

        RoomOccupancyType occupancyType,

        RoomComfortLevel comfortLevel,

        @Min(value = 1, message = "Capacity must be positive")
        Integer capacity,

        Boolean active
) {
}
