package ru.lvrmmm.hotelbookingservice.booking.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.lvrmmm.hotelbookingservice.booking.dto.request.CreateBookingRequest;
import ru.lvrmmm.hotelbookingservice.booking.dto.response.BookingResponse;
import ru.lvrmmm.hotelbookingservice.booking.service.BookingService;
import ru.lvrmmm.hotelbookingservice.security.UserDetailsImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "API для управления бронированиями")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping()
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        BookingResponse response = bookingService.createBooking(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable("id")UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        BookingResponse response = bookingService.getBookingById(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        List<BookingResponse> response = bookingService.getBookingsByUser(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        BookingResponse response = bookingService.cancelBooking(id, userDetails.getId());
        return ResponseEntity.ok(response);
    }

}
