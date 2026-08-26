package ru.lvrmmm.hotelbookingservice.room.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lvrmmm.hotelbookingservice.room.dto.CreateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.dto.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room existingRoom;

    @BeforeEach
    void setUp(){
        existingRoom = new Room(101, BigDecimal.valueOf(1000.00), RoomOccupancyType.SINGLE,
                RoomComfortLevel.STANDARD, 1, true);
        existingRoom.setId(1L);
    }

    @Test
    void getRoomById_shouldReturnRoom_whenRoomExists(){

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));

        RoomResponse response = roomService.getRoomById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.roomNumber()).isEqualTo(101);
        assertThat(response.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(response.occupancyType()).isEqualTo(RoomOccupancyType.SINGLE);
        assertThat(response.comfortLevel()).isEqualTo(RoomComfortLevel.STANDARD);
        assertThat(response.capacity()).isEqualTo(1);
        assertThat(response.active()).isTrue();
    }

    @Test
    void createRoom_shouldSaveAndReturnRoom(){
        CreateRoomRequest request = new CreateRoomRequest(
                102,
                BigDecimal.valueOf(1200.0),
                RoomOccupancyType.DOUBLE,
                RoomComfortLevel.DELUXE,
                2
        );

        Room savedRoom = new Room(102,BigDecimal.valueOf(1200.0),RoomOccupancyType.DOUBLE, RoomComfortLevel.DELUXE,2, true);
        savedRoom.setId(2L);

        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        RoomResponse response = roomService.createRoom(request);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.roomNumber()).isEqualTo(102);
        assertThat(response.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(1200.00));
        assertThat(response.occupancyType()).isEqualTo(RoomOccupancyType.DOUBLE);
        assertThat(response.comfortLevel()).isEqualTo(RoomComfortLevel.DELUXE);
        assertThat(response.capacity()).isEqualTo(2);
        assertThat(response.active()).isTrue();

        verify(roomRepository, times(1)).save(any(Room.class));
    }
}
