package com.omkar.app.dto;

import com.omkar.app.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {
    private Long bookingId;
    private Long showId;
    private String customerEmail;
    private List<String> seatNumbers;
    private Double totalAmount;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime holdExpiresAt;
}
