package com.omkar.app.payment;

import com.omkar.app.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class UPIPaymentStrategy implements PaymentStrategy {

    private static final Pattern UPI_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9]+$");

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.UPI;
    }

    @Override
    public PaymentResult processPayment(double amount, PaymentDetails details) {
        if (details == null || details.getUpiId() == null || details.getUpiId().isBlank()) {
            return PaymentResult.failure("UPI ID is required");
        }
        String upiId = details.getUpiId().trim();
        if (!UPI_ID_PATTERN.matcher(upiId).matches()) {
            return PaymentResult.failure("Invalid UPI ID format (e.g. user@paytm, name@ybl)");
        }
        if (amount <= 0) {
            return PaymentResult.failure("Amount must be positive");
        }

        String transactionId = "UPI-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String masked = maskUpiId(upiId);
        return PaymentResult.success(transactionId, masked);
    }

    private String maskUpiId(String upiId) {
        if (upiId == null || upiId.length() < 3) {
            return "***";
        }
        int at = upiId.indexOf('@');
        if (at <= 0) {
            return "***" + (upiId.length() > 4 ? upiId.substring(upiId.length() - 4) : "");
        }
        String local = upiId.substring(0, at);
        String domain = upiId.substring(at);
        String maskedLocal = local.charAt(0) + "***";
        return maskedLocal + domain;
    }
}
