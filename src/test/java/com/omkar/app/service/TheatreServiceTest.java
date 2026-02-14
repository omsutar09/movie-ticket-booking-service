package com.omkar.app.service;

import com.omkar.app.dto.ShowRequestDTO;
import com.omkar.app.entity.Movie;
import com.omkar.app.entity.Show;
import com.omkar.app.entity.Theatre;
import com.omkar.app.repository.MovieRepository;
import com.omkar.app.repository.ShowRepository;
import com.omkar.app.repository.TheatreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TheatreServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private TheatreService theatreService;

    @Test
    void createShow_Success() {
        Long theatreId = 1L;
        Long movieId = 2L;
        ShowRequestDTO request = new ShowRequestDTO();
        request.setMovieId(movieId);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setPrice(250.0);

        Theatre theatre = new Theatre();
        theatre.setId(theatreId);

        Movie movie = new Movie();
        movie.setId(movieId);

        when(theatreRepository.findById(theatreId)).thenReturn(Optional.of(theatre));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> {
            Show show = invocation.getArgument(0);
            show.setId(10L);
            return show;
        });

        Show createdShow = theatreService.createShow(theatreId, request);

        assertNotNull(createdShow);
        assertEquals(10L, createdShow.getId());
        assertEquals(theatre, createdShow.getTheatre());
        assertEquals(movie, createdShow.getMovie());
        verify(showRepository).save(createdShow);
    }
}
