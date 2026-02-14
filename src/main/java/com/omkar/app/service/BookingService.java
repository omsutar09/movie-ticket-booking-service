package com.omkar.app.service;

import com.omkar.app.dto.BookingRequestDTO;
import com.omkar.app.dto.BookingResponseDTO;
import com.omkar.app.dto.PaymentRequestDTO;
import com.omkar.app.entity.*;
import com.omkar.app.payment.PaymentDetails;
import com.omkar.app.exception.BadRequestException;
import com.omkar.app.exception.ResourceNotFoundException;
import com.omkar.app.repository.BookingRepository;
import com.omkar.app.repository.ShowRepository;
import com.omkar.app.repository.ShowSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Value("${booking.hold-ttl-minutes:10}")
    private int holdTtlMinutes;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private PaymentService paymentService;

    /**
     * Creates a booking with seats held (lock) for TTL. Uses pessimistic lock to prevent
     * concurrent booking of the same seats. If payment is provided, processes payment
     * and confirms booking; otherwise returns PENDING_PAYMENT and client must pay within TTL.
     */
    @Transactional
    public BookingResponseDTO bookTickets(BookingRequestDTO request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", request.getShowId()));

        // Lock seats for update so no other transaction can book them concurrently
        List<ShowSeat> seatsToBook = showSeatRepository.findByShowIdAndSeatNumberInForUpdate(
                show.getId(), request.getSeatNumbers());
        if (seatsToBook.size() != request.getSeatNumbers().size()) {
            throw new BadRequestException("One or more seats not found for this show");
        }
        for (ShowSeat seat : seatsToBook) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new BadRequestException("Seat not available: " + seat.getSeatNumber());
            }
        }

        double basePrice = show.getPrice() != null ? show.getPrice() : 0.0;
        double totalAmount = basePrice * seatsToBook.size();
        LocalDateTime holdExpiresAt = LocalDateTime.now().plusMinutes(holdTtlMinutes);

        Booking booking = new Booking();
        booking.setShow(show);
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setTotalAmount(totalAmount);
        booking.setHoldExpiresAt(holdExpiresAt);
        booking = bookingRepository.save(booking);

        for (ShowSeat seat : seatsToBook) {
            seat.setStatus(SeatStatus.HELD);
            showSeatRepository.save(seat);
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setShowSeat(seat);
            booking.getBookingSeats().add(bs);
        }
        bookingRepository.save(booking);

        if (request.getPayment() != null) {
            PaymentDetails details = toPaymentDetails(request.getPayment());
            paymentService.processPaymentForBooking(booking.getId(), details);
        }
        return toResponseDTO(booking);
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

    @Transactional
    public List<BookingResponseDTO> bulkBook(List<BookingRequestDTO> requests) {
        List<BookingResponseDTO> results = new ArrayList<>();
        for (BookingRequestDTO req : requests) {
            try {
                results.add(bookTickets(req));
            } catch (Exception e) {
                throw new BadRequestException("Bulk booking failed: " + e.getMessage());
            }
        }
        return results;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Booking is already cancelled or expired");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        releaseSeats(booking);
        bookingRepository.save(booking);
    }

    @Transactional
    public void bulkCancel(List<Long> bookingIds) {
        for (Long id : bookingIds) {
            cancelBooking(id);
        }
    }

    public BookingResponseDTO getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        return toResponseDTO(booking);
    }

    private BookingResponseDTO toResponseDTO(Booking booking) {
        List<String> seatNumbers = booking.getBookingSeats().stream()
                .map(bs -> bs.getShowSeat().getSeatNumber())
                .collect(Collectors.toList());
        return new BookingResponseDTO(
                booking.getId(),
                booking.getShow().getId(),
                booking.getCustomerEmail(),
                seatNumbers,
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getHoldExpiresAt()
        );
    }

    /** Releases seats (HELD or BOOKED) back to AVAILABLE. */
    private void releaseSeats(Booking booking) {
        for (BookingSeat bs : booking.getBookingSeats()) {
            ShowSeat seat = bs.getShowSeat();
            seat.setStatus(SeatStatus.AVAILABLE);
            showSeatRepository.save(seat);
        }
    }

    /** Releases expired seat holds (TTL elapsed without payment). Runs every minute. */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expired = bookingRepository.findByStatusAndHoldExpiresAtBefore(
                BookingStatus.PENDING_PAYMENT, now);
        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.EXPIRED);
            releaseSeats(booking);
            bookingRepository.save(booking);
        }
    }
}
