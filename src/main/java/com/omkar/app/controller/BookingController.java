package com.omkar.app.controller;

import com.omkar.app.dto.BookingRequestDTO;
import com.omkar.app.dto.BookingResponseDTO;
import com.omkar.app.dto.BulkBookingRequestDTO;
import com.omkar.app.dto.BulkCancellationRequestDTO;
import com.omkar.app.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> bookTickets(@Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookTickets(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<BookingResponseDTO>> bulkBook(@Valid @RequestBody BulkBookingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bulkBook(request.getBookings()));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel/bulk")
    public ResponseEntity<Void> bulkCancel(@Valid @RequestBody BulkCancellationRequestDTO request) {
        bookingService.bulkCancel(request.getBookingIds());
        return ResponseEntity.noContent().build();
    }
}
