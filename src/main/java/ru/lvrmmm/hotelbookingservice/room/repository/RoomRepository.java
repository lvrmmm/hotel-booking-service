package ru.lvrmmm.hotelbookingservice.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
