package com.omkar.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheatreShowsDTO {
    private Long theatreId;
    private String theatreName;
    private String cityName;
    private List<ShowTimingDTO> shows;
}
