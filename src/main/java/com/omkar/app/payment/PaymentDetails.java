package com.omkar.app.payment;

import com.omkar.app.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    private PaymentMethod paymentMethod;

    // Credit card
    private String cardNumber;
    private String cardExpiry;
    private String cardCvv;
    private String cardHolderName;

    // UPI
    private String upiId;
}
