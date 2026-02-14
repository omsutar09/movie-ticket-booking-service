package com.omkar.app.service;

import com.omkar.app.dto.TheatreShowsDTO;
import com.omkar.app.entity.City;
import com.omkar.app.entity.Movie;
import com.omkar.app.entity.Show;
import com.omkar.app.entity.Theatre;
import com.omkar.app.repository.ShowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrowseServiceTest {

    @Mock
    private ShowRepository showRepository;

    @InjectMocks
    private BrowseService browseService;

    @Test
    void shouldReturnShowsGroupedByTheatre() {
        String cityName = "Mumbai";
        String movieTitle = "Inception";
        LocalDate date = LocalDate.now();
        LocalDateTime startTime = date.atTime(10, 0);

        City city = new City(1L, cityName);
        Theatre theatre = new Theatre(1L, "PVR", city);
        Movie movie = new Movie(1L, movieTitle, "English", "Sci-Fi");
        Show show = new Show(1L, theatre, movie, startTime, startTime.plusHours(2), 250.0);

        when(showRepository.findShowsByCityAndMovieAndDate(eq(cityName), eq(movieTitle), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(show));

        List<TheatreShowsDTO> result = browseService.browseShows(cityName, movieTitle, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        TheatreShowsDTO strings = result.get(0);
        assertEquals(theatre.getId(), strings.getTheatreId());
        assertEquals(theatre.getName(), strings.getTheatreName());
        assertEquals(city.getName(), strings.getCityName());
        assertEquals(1, strings.getShows().size());
        assertEquals(show.getId(), strings.getShows().get(0).getShowId());
    }

    @Test
    void shouldReturnEmptyListWhenNoShowsFound() {
        String cityName = "Delhi";
        String movieTitle = "Matrix";
        LocalDate date = LocalDate.now();

        when(showRepository.findShowsByCityAndMovieAndDate(eq(cityName), eq(movieTitle), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        List<TheatreShowsDTO> result = browseService.browseShows(cityName, movieTitle, date);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
