package com.omkar.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkBookingRequestDTO {
    @NotEmpty(message = "At least one booking is required")
    @Valid
    private List<BookingRequestDTO> bookings;
}
