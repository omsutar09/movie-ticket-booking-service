package com.omkar.app.dto;

import com.omkar.app.entity.PaymentMethod;
import com.omkar.app.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private Long paymentId;
    private Long bookingId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private String maskedRef;
    private LocalDateTime createdAt;
}
