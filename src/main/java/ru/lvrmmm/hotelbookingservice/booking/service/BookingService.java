package ru.lvrmmm.hotelbookingservice.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.booking.dto.request.CreateBookingRequest;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.BookingResponse;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;
import ru.lvrmmm.hotelbookingservice.booking.exception.BookingConflictException;
import ru.lvrmmm.hotelbookingservice.booking.exception.BookingNotFoundException;
import ru.lvrmmm.hotelbookingservice.booking.exception.InvalidBookingDatesException;
import ru.lvrmmm.hotelbookingservice.booking.repository.BookingRepository;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.exception.UserNotFoundException;
import ru.lvrmmm.hotelbookingservice.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public BookingService(UserRepository userRepository, RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, UUID userId) {
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new InvalidBookingDatesException("Check-out date must be after check-in date");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(), request.checkIn(), request.checkOut(), ACTIVE_STATUSES);

        if (!overlappingBookings.isEmpty()) {
            throw new BookingConflictException(
                    "Room " + room.getRoomNumber() + " is already booked for the selected dates");
        }

        BigDecimal totalPrice = calculateTotalPrice(room, request.checkIn(), request.checkOut());

        Booking booking = new Booking(user, room, request.checkIn(), request.checkOut(), totalPrice);
        Booking saved = bookingRepository.save(booking);

        return BookingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id, UUID userId, boolean isStaff) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        boolean isOwner = booking.getUser().getId().equals(userId);
        if (!isOwner && !isStaff) {
            throw new BookingNotFoundException(id);
        }

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUser_Id(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(UUID id, UUID userId, boolean isStaff) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        boolean isOwner = booking.getUser().getId().equals(userId);
        if (!isOwner && !isStaff) {
            throw new BookingConflictException("You can only cancel your own bookings");
        }

        if (booking.getBookingStatus() != BookingStatus.PENDING
                && booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BookingConflictException(
                    "Booking cannot be cancelled from status " + booking.getBookingStatus()
            );
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);

        return BookingResponse.from(updated);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingResponse::from)
                .toList();
    }

    private BigDecimal calculateTotalPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
    }
}