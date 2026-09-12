package ru.lvrmmm.hotelbookingservice.room.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lvrmmm.hotelbookingservice.room.dto.request.CreateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.dto.response.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.dto.request.UpdateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;

import java.util.List;


@Service
public class RoomService {
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
}
