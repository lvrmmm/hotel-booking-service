package ru.lvrmmm.hotelbookingservice.room.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;

import java.math.BigDecimal;

public record CreateRoomRequest(

        @NotNull(message = "Room number is required")
        @Min(value = 1, message = "Room number must be positive")
        Integer roomNumber,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal pricePerNight,

        @NotNull(message = "Occupancy type is required")
        RoomOccupancyType occupancyType,

        @NotNull(message = "Comfort level is required")
        RoomComfortLevel comfortLevel,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be positive")
        Integer capacity
) {
    public Room toEntity(){
        return new Room (roomNumber, pricePerNight, occupancyType, comfortLevel, capacity, true);
    }
}
