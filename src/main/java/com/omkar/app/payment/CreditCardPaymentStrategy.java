package com.omkar.app.payment;

import com.omkar.app.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final int MASKED_DIGITS = 4;
    private static final int MIN_CARD_LENGTH = 13;
    private static final int MAX_CARD_LENGTH = 19;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult processPayment(double amount, PaymentDetails details) {
        if (details == null || details.getCardNumber() == null || details.getCardNumber().isBlank()) {
            return PaymentResult.failure("Card number is required");
        }
        String cardNumber = details.getCardNumber().replaceAll("\\s", "");
        if (cardNumber.length() < MIN_CARD_LENGTH || cardNumber.length() > MAX_CARD_LENGTH) {
            return PaymentResult.failure("Invalid card number length");
        }
        if (!cardNumber.matches("\\d+")) {
            return PaymentResult.failure("Card number must contain only digits");
        }
        if (amount <= 0) {
            return PaymentResult.failure("Amount must be positive");
        }

        String transactionId = "CC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String masked = maskCardNumber(cardNumber);
        return PaymentResult.success(transactionId, masked);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < MASKED_DIGITS) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - MASKED_DIGITS);
        return "****" + lastFour;
    }
}
