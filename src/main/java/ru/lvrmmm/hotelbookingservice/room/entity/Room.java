package ru.lvrmmm.hotelbookingservice.room.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    private int roomNumber;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Enumerated(EnumType.STRING)
    @Column(name = "occupancy_type", nullable = false)
    private RoomOccupancyType occupancyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "comfort_level", nullable = false)
    private RoomComfortLevel comfortLevel;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "active", nullable = false)
    private boolean active;

    public Room() {
    }

    public Room(int roomNumber,
                BigDecimal pricePerNight,
                RoomOccupancyType occupancyType,
                RoomComfortLevel comfortLevel,
                int capacity, boolean active) {
        this.roomNumber = roomNumber;
        this.pricePerNight = pricePerNight;
        this.occupancyType = occupancyType;
        this.comfortLevel = comfortLevel;
        this.capacity = capacity;
        this.active = active;
    }

    public Room(Long id, int roomNumber, BigDecimal pricePerNight, RoomOccupancyType occupancyType, RoomComfortLevel comfortLevel, int capacity, boolean active) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.pricePerNight = pricePerNight;
        this.occupancyType = occupancyType;
        this.comfortLevel = comfortLevel;
        this.capacity = capacity;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public RoomOccupancyType getOccupancyType() {
        return occupancyType;
    }

    public void setOccupancyType(RoomOccupancyType occupancyType) {
        this.occupancyType = occupancyType;
    }

    public RoomComfortLevel getComfortLevel() {
        return comfortLevel;
    }

    public void setComfortLevel(RoomComfortLevel comfortLevel) {
        this.comfortLevel = comfortLevel;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
