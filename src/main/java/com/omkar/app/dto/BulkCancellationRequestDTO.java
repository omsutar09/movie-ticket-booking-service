package com.omkar.app.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkCancellationRequestDTO {
    @NotEmpty(message = "At least one booking ID is required")
    private List<Long> bookingIds;
}
