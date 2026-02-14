package com.omkar.app.service;

import com.omkar.app.dto.ShowTimingDTO;
import com.omkar.app.dto.TheatreShowsDTO;
import com.omkar.app.entity.Show;
import com.omkar.app.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BrowseService {

    @Autowired
    private ShowRepository showRepository;

    @Transactional(readOnly = true)
    public List<TheatreShowsDTO> browseShows(String cityName, String movieTitle, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Show> shows = showRepository.findShowsByCityAndMovieAndDate(cityName, movieTitle, start, end);

        Map<Long, List<Show>> showsByTheatre = shows.stream()
                .collect(Collectors.groupingBy(show -> show.getTheatre().getId()));

        return showsByTheatre.values().stream()
                .map(this::mapToTheatreShowsDTO)
                .collect(Collectors.toList());
    }

    private TheatreShowsDTO mapToTheatreShowsDTO(List<Show> shows) {
        if (shows.isEmpty())
            return null;

        Show firstShow = shows.get(0);
        TheatreShowsDTO dto = new TheatreShowsDTO();
        dto.setTheatreId(firstShow.getTheatre().getId());
        dto.setTheatreName(firstShow.getTheatre().getName());
        dto.setCityName(firstShow.getTheatre().getCity().getName());

        List<ShowTimingDTO> timings = shows.stream()
                .map(show -> new ShowTimingDTO(show.getId(), show.getStartTime(), show.getEndTime(), show.getPrice()))
                .collect(Collectors.toList());

        dto.setShows(timings);
        return dto;
    }
}
