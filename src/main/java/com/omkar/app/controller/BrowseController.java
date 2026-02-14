package com.omkar.app.controller;

import com.omkar.app.dto.TheatreShowsDTO;
import com.omkar.app.service.BrowseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class BrowseController {

    @Autowired
    private BrowseService browseService;

    @GetMapping("/browse")
    public ResponseEntity<List<TheatreShowsDTO>> browseShows(
            @RequestParam String cityName,
            @RequestParam String movieTitle,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(browseService.browseShows(cityName, movieTitle, date));
    }
}
