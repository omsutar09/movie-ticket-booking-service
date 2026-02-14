package com.omkar.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDTO {
    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Valid customer email is required")
    private String customerEmail;

    @NotEmpty(message = "At least one seat is required")
    private List<String> seatNumbers;

    @Valid
    private PaymentRequestDTO payment;
}
