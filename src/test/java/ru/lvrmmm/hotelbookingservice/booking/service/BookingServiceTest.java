package ru.lvrmmm.hotelbookingservice.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import ru.lvrmmm.hotelbookingservice.booking.dto.request.CreateBookingRequest;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.BookingResponse;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;
import ru.lvrmmm.hotelbookingservice.booking.exception.BookingConflictException;
import ru.lvrmmm.hotelbookingservice.booking.exception.BookingNotFoundException;
import ru.lvrmmm.hotelbookingservice.booking.exception.InvalidBookingDatesException;
import ru.lvrmmm.hotelbookingservice.booking.repository.BookingRepository;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;
import ru.lvrmmm.hotelbookingservice.user.entity.User;
import ru.lvrmmm.hotelbookingservice.user.entity.UserRole;
import ru.lvrmmm.hotelbookingservice.user.exception.UserNotFoundException;
import ru.lvrmmm.hotelbookingservice.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    private BookingService bookingService;

    private User existingUser;
    private Room existingRoom;
    private UUID userId;

    @BeforeEach
    void setUp() throws InterruptedException {
        bookingService = new BookingService(userRepository, roomRepository, bookingRepository, redissonClient);

        userId = UUID.randomUUID();

        existingUser = new User("johndoe", "john@example.com", "hashedPassword",
                "John", null, "Doe", LocalDate.of(1990, 1, 1), UserRole.USER);
        existingUser.setId(userId);

        existingRoom = new Room(101, BigDecimal.valueOf(100.00),
                RoomOccupancyType.DOUBLE, RoomComfortLevel.STANDARD, 2, true);
        existingRoom.setId(1L);

        // Redisson: любая блокировка захватывается успешно и сразу.
        // Настроено здесь, а не в каждом тесте отдельно, потому что
        // блокировка захватывается ДО проверки существования user/room —
        // нужна даже для тестов, которые проверяют "не найдено"/"конфликт".
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        lenient().when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
    }

    // ---------- createBooking ----------

    @Test
    void createBooking_shouldCreateBooking_whenDatesAreValidAndRoomIsFree() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));
        when(bookingRepository.findOverlappingBookings(
                eq(1L), eq(request.checkIn()), eq(request.checkOut()), anyList()
        )).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.roomId()).isEqualTo(1L);
        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(500.00));

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(rLock, times(1)).unlock();
    }

    @Test
    void createBooking_shouldThrowException_whenCheckOutBeforeCheckIn() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L, LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 10)
        );

        assertThatThrownBy(() -> bookingService.createBooking(request, userId))
                .isInstanceOf(InvalidBookingDatesException.class);

        verifyNoInteractions(userRepository, roomRepository, bookingRepository, redissonClient);
    }

    @Test
    void createBooking_shouldThrowException_whenUserNotFound() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request, userId))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(roomRepository, bookingRepository);
        verify(rLock, times(1)).unlock();
    }

    @Test
    void createBooking_shouldThrowException_whenRoomNotFound() {
        CreateBookingRequest request = new CreateBookingRequest(
                999L, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request, userId))
                .isInstanceOf(RoomNotFoundException.class);

        verifyNoInteractions(bookingRepository);
        verify(rLock, times(1)).unlock();
    }

    @Test
    void createBooking_shouldThrowConflictException_whenDatesOverlap() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15)
        );

        Booking conflictingBooking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 18), BigDecimal.valueOf(600));

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));
        when(bookingRepository.findOverlappingBookings(
                eq(1L), eq(request.checkIn()), eq(request.checkOut()), anyList()
        )).thenReturn(List.of(conflictingBooking));

        assertThatThrownBy(() -> bookingService.createBooking(request, userId))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("101");

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(rLock, times(1)).unlock();
    }

    @Test
    void createBooking_shouldThrowConflictException_whenLockNotAcquired() throws InterruptedException {
        CreateBookingRequest request = new CreateBookingRequest(
                1L, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15)
        );

        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(request, userId))
                .isInstanceOf(BookingConflictException.class);

        verifyNoInteractions(userRepository, roomRepository, bookingRepository);
        verify(rLock, never()).unlock();
    }

    // ---------- getBookingById ----------

    @Test
    void getBookingById_shouldReturnBooking_whenUserIsOwner() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBookingById(bookingId, userId, false);

        assertThat(response.id()).isEqualTo(bookingId);
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    void getBookingById_shouldThrowNotFound_whenUserIsNotOwnerAndNotStaff() {
        User anotherUser = new User("alice", "alice@example.com", "hash",
                "Alice", null, "Smith", LocalDate.of(1985, 5, 5), UserRole.USER);
        anotherUser.setId(UUID.randomUUID());

        Booking booking = new Booking(anotherUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId, userId, false))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void getBookingById_shouldReturnBooking_whenUserIsNotOwnerButIsStaff() {
        User anotherUser = new User("alice", "alice@example.com", "hash",
                "Alice", null, "Smith", LocalDate.of(1985, 5, 5), UserRole.USER);
        anotherUser.setId(UUID.randomUUID());

        Booking booking = new Booking(anotherUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBookingById(bookingId, userId, true);

        assertThat(response.id()).isEqualTo(bookingId);
    }

    // ---------- getBookingsByUser ----------

    @Test
    void getBookingsByUser_shouldReturnUserBookings() {
        Booking booking1 = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        booking1.setId(UUID.randomUUID());

        when(bookingRepository.findByUser_Id(userId)).thenReturn(List.of(booking1));

        List<BookingResponse> result = bookingService.getBookingsByUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(userId);
    }

    // ---------- cancelBooking ----------

    @Test
    void cancelBooking_shouldCancelBooking_whenOwnerAndPendingStatus() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(bookingId, userId, false);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_shouldThrowConflict_whenNotOwnerAndNotStaff() {
        User anotherUser = new User("alice", "alice@example.com", "hash",
                "Alice", null, "Smith", LocalDate.of(1985, 5, 5), UserRole.USER);
        anotherUser.setId(UUID.randomUUID());

        Booking booking = new Booking(anotherUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, userId, false))
                .isInstanceOf(BookingConflictException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_shouldCancel_whenNotOwnerButIsStaff() {
        User anotherUser = new User("alice", "alice@example.com", "hash",
                "Alice", null, "Smith", LocalDate.of(1985, 5, 5), UserRole.USER);
        anotherUser.setId(UUID.randomUUID());

        Booking booking = new Booking(anotherUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(bookingId, userId, true);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_shouldThrowConflict_whenAlreadyCancelled() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        booking.setBookingStatus(BookingStatus.CANCELLED);
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, userId, false))
                .isInstanceOf(BookingConflictException.class)
                .hasMessageContaining("CANCELLED");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ---------- confirmBooking / completeBooking ----------

    @Test
    void confirmBooking_shouldSetStatusConfirmed_whenPending() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.confirmBooking(bookingId);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBooking_shouldThrowConflict_whenNotPending() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                .isInstanceOf(BookingConflictException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void completeBooking_shouldSetStatusCompleted_whenConfirmed() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.completeBooking(bookingId);

        assertThat(response.bookingStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void completeBooking_shouldThrowConflict_whenNotConfirmed() {
        Booking booking = new Booking(existingUser, existingRoom,
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 15), BigDecimal.valueOf(500));
        UUID bookingId = UUID.randomUUID();
        booking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.completeBooking(bookingId))
                .isInstanceOf(BookingConflictException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}