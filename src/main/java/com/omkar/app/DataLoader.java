package com.omkar.app;

import com.omkar.app.entity.*;
import com.omkar.app.repository.CityRepository;
import com.omkar.app.repository.MovieRepository;
import com.omkar.app.repository.ShowRepository;
import com.omkar.app.repository.ShowSeatRepository;
import com.omkar.app.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Override
    public void run(String... args) throws Exception {
        City mumbai = new City(null, "Mumbai");
        City delhi = new City(null, "Delhi");
        cityRepository.save(mumbai);
        cityRepository.save(delhi);

        Movie inception = new Movie(null, "Inception", "English", "Sci-Fi");
        Movie titanic = new Movie(null, "Titanic", "English", "Romance");
        movieRepository.save(inception);
        movieRepository.save(titanic);

        Theatre pvrMumbai = new Theatre(null, "PVR Mumbai", mumbai);
        Theatre imaxDelhi = new Theatre(null, "IMAX Delhi", delhi);
        theatreRepository.save(pvrMumbai);
        theatreRepository.save(imaxDelhi);

        LocalDateTime showTime1 = LocalDate.of(2026, 2, 10).atTime(10, 0);
        LocalDateTime showTime2 = LocalDate.of(2026, 2, 10).atTime(14, 0);

        Show show1 = new Show(null, pvrMumbai, inception, showTime1, showTime1.plusHours(2), 250.0);
        Show show2 = new Show(null, pvrMumbai, inception, showTime2, showTime2.plusHours(2), 300.0);

        showRepository.save(show1);
        showRepository.save(show2);


        for (Show show : List.of(show1, show2)) {
            for (int row = 0; row < 2; row++) {
                char rowLetter = (char) ('A' + row);
                for (int num = 1; num <= 5; num++) {
                    ShowSeat seat = new ShowSeat();
                    seat.setShow(show);
                    seat.setSeatNumber("" + rowLetter + num);
                    seat.setStatus(SeatStatus.AVAILABLE);
                    showSeatRepository.save(seat);
                }
            }
        }

        System.out.println("Data Loaded Successfully");
    }
}
