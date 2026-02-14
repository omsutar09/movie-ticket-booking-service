package com.omkar.app.controller;

import com.omkar.app.dto.PaymentRequestDTO;
import com.omkar.app.dto.PaymentResponseDTO;
import com.omkar.app.entity.Payment;
import com.omkar.app.payment.PaymentDetails;
import com.omkar.app.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<PaymentResponseDTO> payForBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentRequestDTO request) {
        PaymentDetails details = toPaymentDetails(request);
        Payment payment = paymentService.processPaymentForBooking(bookingId, details);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(payment));
    }

    @GetMapping("/{bookingId}/payment")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable Long bookingId) {
        Payment payment = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(toResponseDTO(payment));
    }

    private PaymentDetails toPaymentDetails(PaymentRequestDTO dto) {
        PaymentDetails details = new PaymentDetails();
        details.setPaymentMethod(dto.getPaymentMethod());
        details.setCardNumber(dto.getCardNumber());
        details.setCardExpiry(dto.getCardExpiry());
        details.setCardCvv(dto.getCardCvv());
        details.setCardHolderName(dto.getCardHolderName());
        details.setUpiId(dto.getUpiId());
        return details;
    }

    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getMaskedRef(),
                payment.getCreatedAt()
        );
    }
}
