package ru.lvrmmm.hotelbookingservice.room.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.lvrmmm.hotelbookingservice.availability.service.RoomAvailabilityService;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.OccupiedRangeResponse;
import ru.lvrmmm.hotelbookingservice.common.config.OpenApiConfig;
import ru.lvrmmm.hotelbookingservice.room.dto.request.CreateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.dto.response.RoomResponse;
import ru.lvrmmm.hotelbookingservice.room.dto.request.UpdateRoomRequest;
import ru.lvrmmm.hotelbookingservice.room.service.RoomService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Rooms", description = "API для управления гостиничными номерами")
public class RoomController {

    private final RoomService roomService;
    private final RoomAvailabilityService roomAvailabilityService;

    public RoomController(RoomService roomService, RoomAvailabilityService roomAvailabilityService) {
        this.roomService = roomService;
        this.roomAvailabilityService = roomAvailabilityService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request){
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @Valid @RequestBody UpdateRoomRequest request){
        RoomResponse response =  roomService.updateRoom(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id){
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms(){
        List<RoomResponse> response = roomService.getAllRooms();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id){
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RoomResponse> deactivateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.deactivateRoom(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<RoomResponse> activateRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.activateRoom(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> findAvailableRooms(
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam(required = false) Integer capacity
    ) {
        return ResponseEntity.ok(roomService.findAvailableRooms(checkIn, checkOut, capacity));
    }

    @GetMapping("/{id}/occupied-dates")
    public ResponseEntity<List<OccupiedRangeResponse>> getOccupiedDates(@PathVariable Long id) {
        return ResponseEntity.ok(roomAvailabilityService.getOccupiedDates(id));
    }
}
