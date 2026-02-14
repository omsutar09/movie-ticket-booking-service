package com.omkar.app.dto;

import com.omkar.app.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Credit card (required when paymentMethod = CREDIT_CARD)
    private String cardNumber;
    private String cardExpiry;   // in MM/YY format
    private String cardCvv;
    private String cardHolderName;

    // UPI (required when paymentMethod = UPI)
    private String upiId;
}
