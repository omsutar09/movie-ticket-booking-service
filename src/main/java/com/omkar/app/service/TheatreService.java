package com.omkar.app.service;

import com.omkar.app.dto.ShowRequestDTO;
import com.omkar.app.dto.ShowSeatResponseDTO;
import com.omkar.app.entity.*;
import com.omkar.app.exception.ResourceNotFoundException;
import com.omkar.app.repository.MovieRepository;
import com.omkar.app.repository.ShowRepository;
import com.omkar.app.repository.ShowSeatRepository;
import com.omkar.app.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TheatreService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Transactional
    public Show createShow(Long theatreId, ShowRequestDTO showRequest) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre", theatreId));

        Movie movie = movieRepository.findById(showRequest.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", showRequest.getMovieId()));

        Show show = new Show();
        show.setTheatre(theatre);
        show.setMovie(movie);
        show.setStartTime(showRequest.getStartTime());
        show.setEndTime(showRequest.getEndTime());
        show.setPrice(showRequest.getPrice());

        return showRepository.save(show);
    }

    @Transactional
    public Show updateShow(Long showId, ShowRequestDTO showRequest) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));

        if (showRequest.getMovieId() != null) {
            Movie movie = movieRepository.findById(showRequest.getMovieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Movie", showRequest.getMovieId()));
            show.setMovie(movie);
        }

        if (showRequest.getStartTime() != null)
            show.setStartTime(showRequest.getStartTime());
        if (showRequest.getEndTime() != null)
            show.setEndTime(showRequest.getEndTime());
        if (showRequest.getPrice() != null)
            show.setPrice(showRequest.getPrice());

        return showRepository.save(show);
    }

    @Transactional
    public void deleteShow(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show", showId);
        }
        showRepository.deleteById(showId);
    }

    @Transactional
    public List<ShowSeat> allocateSeats(Long showId, List<String> seatNumbers) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));
        List<ShowSeat> created = new java.util.ArrayList<>();
        for (String seatNumber : seatNumbers) {
            if (showSeatRepository.findByShowIdAndSeatNumber(showId, seatNumber).isEmpty()) {
                ShowSeat seat = new ShowSeat();
                seat.setShow(show);
                seat.setSeatNumber(seatNumber);
                seat.setStatus(SeatStatus.AVAILABLE);
                created.add(showSeatRepository.save(seat));
            }
        }
        return created;
    }

    @Transactional
    public List<ShowSeatResponseDTO> updateSeatInventory(Long showId, List<String> seatNumbersToAdd, List<String> seatNumbersToRemove) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));
        if (seatNumbersToRemove != null) {
            for (String seatNum : seatNumbersToRemove) {
                showSeatRepository.findByShowIdAndSeatNumber(showId, seatNum).ifPresent(seat -> {
                    if (seat.getStatus() == SeatStatus.BOOKED) {
                        throw new com.omkar.app.exception.BadRequestException("Cannot remove booked seat: " + seatNum);
                    }
                    showSeatRepository.delete(seat);
                });
            }
        }
        if (seatNumbersToAdd != null && !seatNumbersToAdd.isEmpty()) {
            allocateSeats(showId, seatNumbersToAdd);
        }
        return getSeatsForShow(showId);
    }

    public List<ShowSeatResponseDTO> getSeatsForShow(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show", showId);
        }
        return showSeatRepository.findByShowId(showId).stream()
                .map(s -> new ShowSeatResponseDTO(s.getId(), s.getSeatNumber(), s.getStatus()))
                .toList();
    }
}
