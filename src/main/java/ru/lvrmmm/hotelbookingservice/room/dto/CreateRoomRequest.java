package ru.lvrmmm.hotelbookingservice.room.dto;

import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;

import java.math.BigDecimal;

public record CreateRoomRequest(
        int roomNumber,
        BigDecimal pricePerNight,
        RoomOccupancyType occupancyType,
        RoomComfortLevel comfortLevel,
        int capacity
) {
    public Room toEntity(){
        return new Room (roomNumber, pricePerNight, occupancyType, comfortLevel, capacity, true);
    }
}
