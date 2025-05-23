package com.fourt.railskylines.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResTicketHistoryDTO {
    private String ticketCode;
    private String name;
    private String citizenId;
    private Long seatId;  
    private String carriageName;
    private String trainName;
    private Double price;
    private String boardingStationName;
    private String alightingStationName;
    private Instant startDay;
}

