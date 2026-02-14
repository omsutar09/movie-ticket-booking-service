package com.omkar.app.service;

import com.omkar.app.dto.BookingRequestDTO;
import com.omkar.app.dto.BookingResponseDTO;
import com.omkar.app.entity.*;
import com.omkar.app.exception.BadRequestException;
import com.omkar.app.repository.BookingRepository;
import com.omkar.app.repository.ShowRepository;
import com.omkar.app.repository.ShowSeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void bookTickets_Success() {
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");
        BookingRequestDTO request = new BookingRequestDTO();
        request.setShowId(showId);
        request.setSeatNumbers(seatNumbers);
        request.setCustomerEmail("test@example.com");

        Show show = new Show();
        show.setId(showId);
        show.setPrice(100.0);

        ShowSeat seat1 = new ShowSeat();
        seat1.setSeatNumber("A1");
        seat1.setStatus(SeatStatus.AVAILABLE);

        ShowSeat seat2 = new ShowSeat();
        seat2.setSeatNumber("A2");
        seat2.setStatus(SeatStatus.AVAILABLE);

        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByShowIdAndSeatNumberInForUpdate(eq(showId), anyList()))
                .thenReturn(List.of(seat1, seat2));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDTO response = bookingService.bookTickets(request);

        assertNotNull(response);
        assertEquals(BookingStatus.PENDING_PAYMENT, response.getStatus());
        assertEquals(200.0, response.getTotalAmount());
        verify(showSeatRepository, times(2)).save(any(ShowSeat.class));
    }

    @Test
    void bookTickets_SeatsNotAvailable() {
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1");
        BookingRequestDTO request = new BookingRequestDTO();
        request.setShowId(showId);
        request.setSeatNumbers(seatNumbers);

        Show show = new Show();
        show.setId(showId);

        ShowSeat seat1 = new ShowSeat();
        seat1.setSeatNumber("A1");
        seat1.setStatus(SeatStatus.BOOKED);

        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(showSeatRepository.findByShowIdAndSeatNumberInForUpdate(eq(showId), anyList()))
                .thenReturn(List.of(seat1));

        assertThrows(BadRequestException.class, () -> bookingService.bookTickets(request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
