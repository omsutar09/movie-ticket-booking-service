package com.omkar.app.payment;

import com.omkar.app.entity.PaymentMethod;

public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    PaymentResult processPayment(double amount, PaymentDetails details);
}
