package com.omkar.app.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SeatAllocateDTO {
    @NotEmpty(message = "At least one seat number is required")
    private List<String> seatNumbers;
}
