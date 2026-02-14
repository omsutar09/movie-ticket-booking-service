package com.omkar.app.service;

import com.omkar.app.entity.Booking;
import com.omkar.app.entity.BookingStatus;
import com.omkar.app.entity.Payment;
import com.omkar.app.entity.PaymentMethod;
import com.omkar.app.entity.PaymentStatus;
import com.omkar.app.payment.PaymentDetails;
import com.omkar.app.payment.PaymentResult;
import com.omkar.app.payment.PaymentContext;
import com.omkar.app.repository.BookingRepository;
import com.omkar.app.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentContext paymentContext;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentForBooking_Success() {
        Long bookingId = 1L;
        PaymentDetails details = new PaymentDetails();
        details.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setTotalAmount(100.0);

        PaymentResult result = new PaymentResult(true, "tx123", "****1234", "Success");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.empty()); // No prior payment
        when(paymentContext.processPayment(eq(PaymentMethod.CREDIT_CARD), eq(100.0), any())).thenReturn(result);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.processPaymentForBooking(bookingId, details);

        assertNotNull(payment);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("tx123", payment.getTransactionId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void processPaymentForBooking_Failure() {
        Long bookingId = 1L;
        PaymentDetails details = new PaymentDetails();
        details.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setTotalAmount(100.0);

        PaymentResult result = new PaymentResult(false, null, null, "Insufficient funds");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());
        when(paymentContext.processPayment(eq(PaymentMethod.CREDIT_CARD), eq(100.0), any())).thenReturn(result);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // The service throws BadRequestException on failure
        Exception exception = assertThrows(Exception.class,
                () -> paymentService.processPaymentForBooking(bookingId, details));
        assertTrue(exception.getMessage().contains("Payment failed"));

        verify(bookingRepository, never()).save(booking);
    }
}
