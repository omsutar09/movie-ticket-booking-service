package com.omkar.app.controller;

import com.omkar.app.dto.SeatAllocateDTO;
import com.omkar.app.dto.ShowRequestDTO;
import com.omkar.app.dto.ShowSeatResponseDTO;
import com.omkar.app.entity.Show;
import com.omkar.app.entity.ShowSeat;
import com.omkar.app.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    @Autowired
    private TheatreService theatreService;

    @PostMapping("/{theatreId}/shows")
    public ResponseEntity<Show> createShow(@PathVariable Long theatreId, @Valid @RequestBody ShowRequestDTO showRequest) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(theatreService.createShow(theatreId, showRequest));
    }

    @PutMapping("/shows/{showId}")
    public ResponseEntity<Show> updateShow(@PathVariable Long showId, @Valid @RequestBody ShowRequestDTO showRequest) {
        return ResponseEntity.ok(theatreService.updateShow(showId, showRequest));
    }

    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<Void> deleteShow(@PathVariable Long showId) {
        theatreService.deleteShow(showId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/shows/{showId}/seats")
    public ResponseEntity<Map<String, Object>> allocateSeats(
            @PathVariable Long showId,
            @Valid @RequestBody SeatAllocateDTO request) {
        List<ShowSeat> created = theatreService.allocateSeats(showId, request.getSeatNumbers());
        List<String> seatNumbers = created.stream().map(ShowSeat::getSeatNumber).toList();
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(Map.of("showId", showId, "seatsAdded", created.size(), "seatNumbers", seatNumbers));
    }

    @PutMapping("/shows/{showId}/seats")
    public ResponseEntity<List<ShowSeatResponseDTO>> updateSeatInventory(
            @PathVariable Long showId,
            @RequestBody Map<String, List<String>> body) {
        List<String> add = body.getOrDefault("add", List.of());
        List<String> remove = body.getOrDefault("remove", List.of());
        return ResponseEntity.ok(theatreService.updateSeatInventory(showId, add, remove));
    }

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<List<ShowSeatResponseDTO>> getShowSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(theatreService.getSeatsForShow(showId));
    }
}
