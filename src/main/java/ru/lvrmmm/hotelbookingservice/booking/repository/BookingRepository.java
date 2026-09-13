package ru.lvrmmm.hotelbookingservice.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.lvrmmm.hotelbookingservice.booking.entity.Booking;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUser_Id(UUID userId);

    List<Booking> findByRoomIdAndBookingStatusIn(Long roomId, List<BookingStatus> statuses);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.room.id = :roomId
            AND b.bookingStatus IN :activeStatuses
            AND b.checkIn < :checkOut
            AND b.checkOut > :checkIn
            """)
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn")LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
            );

}
