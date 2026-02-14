package com.omkar.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowTimingDTO {
    private Long showId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;
}
