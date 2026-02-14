package com.omkar.app.dto;

import com.omkar.app.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowSeatResponseDTO {
    private Long id;
    private String seatNumber;
    private SeatStatus status;
}
