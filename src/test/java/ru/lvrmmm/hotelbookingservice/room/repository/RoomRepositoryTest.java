package ru.lvrmmm.hotelbookingservice.room.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;
import ru.lvrmmm.hotelbookingservice.booking.repository.BookingRepository;
import ru.lvrmmm.hotelbookingservice.integration.AbstractIntegrationTest;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;
import ru.lvrmmm.hotelbookingservice.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RoomRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Room availableRoom;
    private Room occupiedRoom;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();

        availableRoom = roomRepository.save(new Room(
                701, new BigDecimal("4500.00"), RoomOccupancyType.DOUBLE,
                RoomComfortLevel.STANDARD, 2, true));
        occupiedRoom = roomRepository.save(new Room(
                702, new BigDecimal("8000.00"), RoomOccupancyType.DOUBLE,
                RoomComfortLevel.DELUXE, 2, true));

        User user = userRepository.save(new User(
                "repository-test-user", "repository-test@example.com", "hash",
                "Test", null, "User", LocalDate.of(1990, 1, 1), UserRole.USER));

        Booking booking = new Booking(
                user, occupiedRoom, LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 1, 15), new BigDecimal("40000.00"));
        bookingRepository.saveAndFlush(booking);
    }

    @Test
    void findAvailableRooms_excludesRoomWithOverlappingActiveBooking() {
        List<Room> result = roomRepository.findAvailableRooms(
                LocalDate.of(2027, 1, 12),
                LocalDate.of(2027, 1, 14),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(result)
                .extracting(Room::getId)
                .contains(availableRoom.getId())
                .doesNotContain(occupiedRoom.getId());
    }

    @Test
    void findAvailableRooms_allowsAdjacentBookingDates() {
        List<Room> result = roomRepository.findAvailableRooms(
                LocalDate.of(2027, 1, 15),
                LocalDate.of(2027, 1, 18),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        assertThat(result)
                .extracting(Room::getId)
                .contains(availableRoom.getId(), occupiedRoom.getId());
    }
}
