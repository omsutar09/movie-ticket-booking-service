package com.omkar.app.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String message;
    private String maskedPaymentRef;

    public static PaymentResult success(String transactionId, String maskedPaymentRef) {
        return new PaymentResult(true, transactionId, "Payment successful", maskedPaymentRef);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message, null);
    }
}
