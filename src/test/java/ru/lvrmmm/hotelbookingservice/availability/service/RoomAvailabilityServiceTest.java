package ru.lvrmmm.hotelbookingservice.availability.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.OccupiedRangeResponse;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.repository.BookingRepository;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;
import ru.lvrmmm.hotelbookingservice.room.exception.InvalidDateRangeException;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomAvailabilityService roomAvailabilityService;

    private Room room;
    private User user;

    private Room buildRoom() {
        Room r = new Room(101, BigDecimal.valueOf(100.00),
                RoomOccupancyType.DOUBLE, RoomComfortLevel.STANDARD, 2, true);
        r.setId(1L);
        return r;
    }

    // ---------- getOccupiedDates ----------

    @Test
    void getOccupiedDates_shouldReturnSortedRanges_whenBookingsExist() {
        // given
        room = buildRoom();
        user = new User("johndoe", "john@example.com", "hash", "John", null, "Doe",
                LocalDate.of(1990, 1, 1), UserRole.USER);

        Booking laterBooking = new Booking(user, room,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 25), BigDecimal.valueOf(500));
        Booking earlierBooking = new Booking(user, room,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), BigDecimal.valueOf(400));

        when(roomRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByRoomIdAndBookingStatusIn(eq(1L), anyList()))
                .thenReturn(List.of(laterBooking, earlierBooking));

        // when
        List<OccupiedRangeResponse> result = roomAvailabilityService.getOccupiedDates(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).checkIn()).isEqualTo(LocalDate.of(2026, 12, 1)); // раньше идёт первым
        assertThat(result.get(1).checkIn()).isEqualTo(LocalDate.of(2026, 12, 20));
    }

    @Test
    void getOccupiedDates_shouldThrowException_whenRoomNotFound() {
        // given
        when(roomRepository.existsById(999L)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> roomAvailabilityService.getOccupiedDates(999L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void getOccupiedDates_shouldReturnEmptyList_whenNoActiveBookings() {
        // given
        when(roomRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByRoomIdAndBookingStatusIn(eq(1L), anyList()))
                .thenReturn(List.of());

        // when
        List<OccupiedRangeResponse> result = roomAvailabilityService.getOccupiedDates(1L);

        // then
        assertThat(result).isEmpty();
    }

    // ---------- findAvailableRooms ----------

    @Test
    void findAvailableRooms_shouldReturnFilteredRooms_whenCapacitySpecified() {
        // given
        Room smallRoom = new Room(101, BigDecimal.valueOf(80.00),
                RoomOccupancyType.SINGLE, RoomComfortLevel.STANDARD, 1, true);
        smallRoom.setId(1L);
        Room bigRoom = new Room(102, BigDecimal.valueOf(150.00),
                RoomOccupancyType.FAMILY, RoomComfortLevel.DELUXE, 4, true);
        bigRoom.setId(2L);

        LocalDate checkIn = LocalDate.of(2026, 12, 1);
        LocalDate checkOut = LocalDate.of(2026, 12, 5);

        when(roomRepository.findAvailableRooms(eq(checkIn), eq(checkOut), anyList()))
                .thenReturn(List.of(smallRoom, bigRoom));

        // when
        var result = roomAvailabilityService.findAvailableRooms(checkIn, checkOut, 3);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).roomNumber()).isEqualTo(102);
    }

    @Test
    void findAvailableRooms_shouldReturnAll_whenCapacityNotSpecified() {
        // given
        Room r1 = buildRoom();
        LocalDate checkIn = LocalDate.of(2026, 12, 1);
        LocalDate checkOut = LocalDate.of(2026, 12, 5);

        when(roomRepository.findAvailableRooms(eq(checkIn), eq(checkOut), anyList()))
                .thenReturn(List.of(r1));

        // when
        var result = roomAvailabilityService.findAvailableRooms(checkIn, checkOut, null);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void findAvailableRooms_shouldThrowException_whenCheckOutBeforeCheckIn() {
        LocalDate checkIn = LocalDate.of(2026, 12, 10);
        LocalDate checkOut = LocalDate.of(2026, 12, 5);

        assertThatThrownBy(() -> roomAvailabilityService.findAvailableRooms(checkIn, checkOut, null))
                .isInstanceOf(InvalidDateRangeException.class);
    }
}