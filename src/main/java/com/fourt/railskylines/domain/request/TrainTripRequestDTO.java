package com.fourt.railskylines.domain.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainTripRequestDTO {
    @NotNull(message = "Train ID is required")
    private Long trainId;

    @NotEmpty(message = "Origin station name is required")
    private String originStationName;

    @NotEmpty(message = "Journey station names are required")
    private List<String> journeyStationNames;

    @NotNull(message = "Departure time is required")
    private String departureTime;

    @NotNull(message = "Arrival time is required")
    private String arrivalTime;
}