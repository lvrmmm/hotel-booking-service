package ru.lvrmmm.hotelbookingservice.room.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.OccupiedRangeResponse;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;
import ru.lvrmmm.hotelbookingservice.room.dto.request.CreateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.dto.response.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.dto.request.UpdateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.exception.InvalidDateRangeException;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


@Service
public class RoomService {

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request){
        Room room = request.toEntity();
        Room saved = roomRepository.save(room);
        return RoomResponse.from(saved);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request){
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        if (request.roomNumber() != null){
            room.setRoomNumber(request.roomNumber());
        }
        if (request.pricePerNight() != null){
            room.setPricePerNight(request.pricePerNight());
        }
        if(request.occupancyType() != null){
            room.setOccupancyType(request.occupancyType());
        }
        if (request.comfortLevel() != null){
            room.setComfortLevel(request.comfortLevel());
        }
        if (request.capacity() != null){
            room.setCapacity(request.capacity());
        }
        if (request.active() != null){
            room.setActive(request.active());
        }
        Room updatedRoom = roomRepository.save(room);
        return RoomResponse.from(updatedRoom);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id){
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        return RoomResponse.from(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms(){
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional
    public void deleteRoom(Long id){
        int deletedCount = roomRepository.deleteRoomById(id);
        if (deletedCount == 0){
            throw new RoomNotFoundException(id);
        }
    }

    @Transactional
    public RoomResponse deactivateRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        room.setActive(false);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse activateRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        room.setActive(true);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer minCapacity) {
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateRangeException("Check-out date must be after check-in date");
        }

        List<Room> available = roomRepository.findAvailableRooms(checkIn, checkOut, ACTIVE_BOOKING_STATUSES);

        return available.stream()
                .filter(room -> minCapacity == null || room.getCapacity() >= minCapacity)
                .map(RoomResponse::from)
                .toList();
    }
}
