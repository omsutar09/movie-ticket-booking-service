package com.omkar.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShowRequestDTO {
    private Long movieId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;
}
