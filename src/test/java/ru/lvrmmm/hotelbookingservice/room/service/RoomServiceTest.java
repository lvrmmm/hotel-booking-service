package ru.lvrmmm.hotelbookingservice.room.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lvrmmm.hotelbookingservice.room.dto.CreateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.dto.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.dto.UpdateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.entity.Room;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomComfortLevel;
import ru.lvrmmm.hotelbookingservice.room.entity.RoomOccupancyType;
import ru.lvrmmm.hotelbookingservice.room.exception.RoomNotFoundException;
import ru.lvrmmm.hotelbookingservice.room.repository.RoomRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void createRoom_shouldSaveAndReturnRoom_whenValidData(){
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

    @Test
    void getRoomById_shouldThrowRoomNotFoundException_whenRoomDoesNotExist(){

        when(roomRepository.findById((99L))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(99L)).isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void getAllRooms_shouldReturnListOfRooms_whenRoomsExist(){
        Room secondRoom = new Room(102, BigDecimal.valueOf(2000.00), RoomOccupancyType.DOUBLE,
                RoomComfortLevel.DELUXE, 2, true);
        secondRoom.setId(2L);

        when(roomRepository.findAll()).thenReturn(List.of(existingRoom, secondRoom));

        List<RoomResponse> response = roomService.getAllRooms();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).id()).isEqualTo(1L);
        assertThat(response.get(1).id()).isEqualTo(2L);
    }
    @Test
    void getAllRooms_shouldReturnEmptyList_whenNoRoomsExist() {

        when(roomRepository.findAll()).thenReturn(Collections.emptyList());

        List<RoomResponse> response = roomService.getAllRooms();

        assertThat(response).isEmpty();
    }

    @Test
    void updateRoom_shouldUpdateAllFields_whenAllFieldsProvided() {

        UpdateRoomRequest request = new UpdateRoomRequest(
                202,
                BigDecimal.valueOf(5000.00),
                RoomOccupancyType.DOUBLE,
                RoomComfortLevel.DELUXE,
                2,
                false
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse response = roomService.updateRoom(1L, request);

        assertThat(response.roomNumber()).isEqualTo(202);
        assertThat(response.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(response.occupancyType()).isEqualTo(RoomOccupancyType.DOUBLE);
        assertThat(response.comfortLevel()).isEqualTo(RoomComfortLevel.DELUXE);
        assertThat(response.capacity()).isEqualTo(2);
        assertThat(response.active()).isFalse();

        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void updateRoom_shouldUpdateOnlyProvidedFields_whenPartialData() {

        UpdateRoomRequest request = new UpdateRoomRequest(
                null,
                BigDecimal.valueOf(2500.00),
                null,
                null,
                null,
                null
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponse response = roomService.updateRoom(1L, request);

        assertThat(response.pricePerNight()).isEqualByComparingTo(BigDecimal.valueOf(2500.00));

        assertThat(response.roomNumber()).isEqualTo(101);
        assertThat(response.occupancyType()).isEqualTo(RoomOccupancyType.SINGLE);
        assertThat(response.comfortLevel()).isEqualTo(RoomComfortLevel.STANDARD);
        assertThat(response.capacity()).isEqualTo(1);
        assertThat(response.active()).isTrue();
    }

    @Test
    void updateRoom_shouldThrowRoomNotFoundException_whenRoomDoesNotExist() {

        UpdateRoomRequest request = new UpdateRoomRequest(
                null, BigDecimal.valueOf(2500.00), null, null, null, null
        );
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> roomService.updateRoom(99L, request))
                .isInstanceOf(RoomNotFoundException.class);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void deleteRoom_shouldDeleteRoom_whenRoomExists() {

        when(roomRepository.deleteRoomById(1L)).thenReturn(1);

        roomService.deleteRoom(1L);

        verify(roomRepository, times(1)).deleteRoomById(1L);
    }

    @Test
    void deleteRoom_shouldThrowRoomNotFoundException_whenRoomDoesNotExist() {

        when(roomRepository.deleteRoomById(99L)).thenReturn(0);

        assertThatThrownBy(() -> roomService.deleteRoom(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }
}
