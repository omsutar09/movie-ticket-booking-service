package com.omkar.app.service;

import com.omkar.app.entity.Booking;
import com.omkar.app.entity.BookingStatus;
import com.omkar.app.entity.Payment;
import com.omkar.app.entity.PaymentStatus;
import com.omkar.app.entity.SeatStatus;
import com.omkar.app.exception.BadRequestException;
import com.omkar.app.exception.ResourceNotFoundException;
import com.omkar.app.payment.PaymentContext;
import com.omkar.app.payment.PaymentDetails;
import com.omkar.app.payment.PaymentResult;
import com.omkar.app.repository.BookingRepository;
import com.omkar.app.repository.PaymentRepository;
import com.omkar.app.repository.ShowSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentContext paymentContext;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Transactional
    public Payment processPaymentForBooking(Long bookingId, PaymentDetails details) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Cannot pay for a cancelled or expired booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Booking is not awaiting payment");
        }
        LocalDateTime now = LocalDateTime.now();
        if (booking.getHoldExpiresAt() != null && now.isAfter(booking.getHoldExpiresAt())) {
            throw new BadRequestException("Seat hold expired. Please create a new booking.");
        }
        paymentRepository.findByBookingId(bookingId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                throw new BadRequestException("Booking is already paid");
            }
        });

        double amount = booking.getTotalAmount();
        PaymentResult result = paymentContext.processPayment(details.getPaymentMethod(), amount, details);

        Payment payment = paymentRepository.findByBookingId(bookingId).orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setBooking(booking);
            newPayment.setAmount(amount);
            newPayment.setPaymentMethod(details.getPaymentMethod());
            newPayment.setStatus(PaymentStatus.PENDING);
            return paymentRepository.save(newPayment);
        });

        if (result.isSuccess()) {
            payment.setTransactionId(result.getTransactionId());
            payment.setMaskedRef(result.getMaskedPaymentRef());
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            confirmBookingAfterPayment(booking);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new BadRequestException("Payment failed: " + result.getMessage());
        }
        return payment;
    }

    private void confirmBookingAfterPayment(Booking booking) {
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setHoldExpiresAt(null);
        booking.getBookingSeats().forEach(bs -> {
            bs.getShowSeat().setStatus(SeatStatus.BOOKED);
            showSeatRepository.save(bs.getShowSeat());
        });
        bookingRepository.save(booking);
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for booking: " + bookingId));
    }
}
