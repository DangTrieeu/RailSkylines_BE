package com.fourt.RailSkylines.controller;

import org.springframework.web.bind.annotation.RestController;

import com.fourt.RailSkylines.domain.Train;
import com.fourt.RailSkylines.service.TrainService;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/trains")
public class TrainController {
    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping("/find/result")
    public ResponseEntity<List<Train>> getTrains(
            @RequestParam String departureStationName,
            @RequestParam String arrivalStationName,
            @RequestParam Instant departureDate) {
        List<Train> results = this.trainService.findTrainsByStationNamesAndDate(
                departureStationName,
                arrivalStationName,
                departureDate);
        return ResponseEntity.ok().body(results);
    }
}
