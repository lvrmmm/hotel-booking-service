package ru.lvrmmm.hotelbookingservice.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.lvrmmm.hotelbookingservice.booking.entity.BookingStatus;
import ru.lvrmmm.hotelbookingservice.room.dto.response.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Modifying
    @Query("DELETE FROM Room r WHERE r.id = :id")
    int deleteRoomById(@Param("id")Long id);


    @Query("""
        SELECT r FROM Room r
        WHERE r.active = true
        AND r.id NOT IN (
            SELECT b.room.id FROM Booking b
            WHERE b.bookingStatus IN :activeStatuses
            AND b.checkIn < :checkOut
            AND b.checkOut > :checkIn
        )
        """)
    List<Room> findAvailableRooms(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );


}
