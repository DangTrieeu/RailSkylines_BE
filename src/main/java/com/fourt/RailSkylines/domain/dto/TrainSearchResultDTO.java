package com.fourt.RailSkylines.domain.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainSearchResultDTO {
    private Long trainID;
    private String trainName;
    private Instant departure;
    private Instant arrival;
}
