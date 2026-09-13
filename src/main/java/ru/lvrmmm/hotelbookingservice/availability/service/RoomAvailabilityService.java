package ru.lvrmmm.hotelbookingservice.availability.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.OccupiedRangeResponse;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;
import ru.lvrmmm.hotelbookingservice.booking.repository.BookingRepository;
import ru.lvrmmm.hotelbookingservice.room.dto.response.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.exception.InvalidDateRangeException;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class RoomAvailabilityService {

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomAvailabilityService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<OccupiedRangeResponse> getOccupiedDates(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RoomNotFoundException(roomId);
        }

        return bookingRepository.findByRoomIdAndBookingStatusIn(roomId, ACTIVE_STATUSES).stream()
                .sorted(Comparator.comparing(Booking::getCheckIn))
                .map(b -> new OccupiedRangeResponse(b.getCheckIn(), b.getCheckOut()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer minCapacity) {
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDateRangeException(
                    "Check-out date must be after check-in date"
            );
        }

        List<Room> available = roomRepository.findAvailableRooms(checkIn, checkOut, ACTIVE_STATUSES);

        return available.stream()
                .filter(room -> minCapacity == null || room.getCapacity() >= minCapacity)
                .map(RoomResponse::from)
                .toList();
    }
}