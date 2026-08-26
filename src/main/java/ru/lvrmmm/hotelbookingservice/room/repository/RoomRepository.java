package ru.lvrmmm.hotelbookingservice.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Modifying
    @Query("DELETE FROM Room WHERE r.id = :id")
    public int deleteRoomById(@Param("id")Long id);

}
