package ru.lvrmmm.hotelbookingservice.room.dto;

import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        int roomNumber,
        BigDecimal pricePerNight,
        RoomOccupancyType occupancyType,
        RoomComfortLevel comfortLevel,
        int capacity,
        boolean active
) {
    public static RoomResponse from(Room room){
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getPricePerNight(),
                room.getOccupancyType(),
                room.getComfortLevel(),
                room.getCapacity(),
                room.isActive()
        );
    }
}

