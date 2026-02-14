package com.omkar.app.payment;

import com.omkar.app.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentContext {

    private final Map<PaymentMethod, PaymentStrategy> strategiesByMethod;

    public PaymentContext(List<PaymentStrategy> strategies) {
        this.strategiesByMethod = strategies.stream()
                .collect(Collectors.toMap(PaymentStrategy::getPaymentMethod, Function.identity()));
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategiesByMethod.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        return strategy;
    }

    public PaymentResult processPayment(PaymentMethod method, double amount, PaymentDetails details) {
        return getStrategy(method).processPayment(amount, details);
    }
}
