package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Route;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.Station;
import com.fourt.railskylines.domain.TrainTrip;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.repository.StationRepository;
import com.fourt.railskylines.repository.TrainTripRepository;
import com.fourt.railskylines.service.SeatService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1")
public class SeatController {

    private final SeatService seatService;
    private final StationRepository stationRepository;
    private final TrainTripRepository trainTripRepository;

    public SeatController(SeatService seatService, StationRepository stationRepository, TrainTripRepository trainTripRepository) {
        this.seatService = seatService;
        this.stationRepository = stationRepository;
        this.trainTripRepository = trainTripRepository;
    }

    @PostMapping("/seats")
    @APIMessage("Create a new seat")
    public ResponseEntity<Seat> createNewSeat(@Valid @RequestBody Seat seat) throws IdInvalidException {
        Seat savedSeat = this.seatService.handleCreateSeat(seat);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSeat);
    }

    @GetMapping("/seats")
    @APIMessage("Fetch all seats")
    public ResponseEntity<ResultPaginationDTO> getAllSeats(
            @Filter Specification<Seat> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.seatService.fetchAllSeats(spec, pageable));
    }

    @GetMapping("/seats/{id}")
    @APIMessage("Fetch seat by ID")
    public ResponseEntity<Seat> getSeatById(@PathVariable("id") Long id) throws IdInvalidException {
        Seat seat = this.seatService.fetchSeatById(id);
        if (seat == null) {
            throw new IdInvalidException("Seat with id = " + id + " does not exist, please check again");
        }
        return ResponseEntity.ok().body(seat);
    }

    @PutMapping("/seats/{id}")
    @APIMessage("Update seat by ID")
    public ResponseEntity<Seat> handleUpdateSeat(
            @PathVariable("id") Long id,
            @Valid @RequestBody Seat seat) throws IdInvalidException {
        Seat existingSeat = this.seatService.fetchSeatById(id);
        if (existingSeat == null) {
            throw new IdInvalidException("Seat with id = " + id + " does not exist, please check again");
        }
        Seat updatedSeat = this.seatService.handleUpdateSeat(id, seat);
        return ResponseEntity.ok(updatedSeat);
    }

    @DeleteMapping("/seats/{id}")
    @APIMessage("Delete seat by ID")
    public ResponseEntity<String> deleteSeat(@PathVariable("id") Long id) throws IdInvalidException {
        Seat existingSeat = this.seatService.fetchSeatById(id);
        if (existingSeat == null) {
            throw new IdInvalidException("Seat with id = " + id + " does not exist");
        }
        this.seatService.handleDeleteSeat(id);
        return ResponseEntity.ok("Delete Success");
    }

    @GetMapping("/seats/available")
    @APIMessage("Fetch available seats for a segment")
    public ResponseEntity<List<Seat>> getAvailableSeatsForSegment(
            @RequestParam Long trainTripId,
            @RequestParam Long boardingStationId,
            @RequestParam Long alightingStationId) {
        Optional<Station> boardingStationOpt = stationRepository.findById(boardingStationId);
        Optional<Station> alightingStationOpt = stationRepository.findById(alightingStationId);
        if (boardingStationOpt.isEmpty()) {
            throw new IllegalArgumentException("Ga lên tàu không tồn tại");
        }
        if (alightingStationOpt.isEmpty()) {
            throw new IllegalArgumentException("Ga xuống tàu không tồn tại");
        }
        Station boardingStation = boardingStationOpt.get();
        Station alightingStation = alightingStationOpt.get();

        Optional<TrainTrip> trainTripOpt = trainTripRepository.findById(trainTripId);
        if (trainTripOpt.isEmpty()) {
            throw new IllegalArgumentException("Chuyến tàu không tồn tại");
        }
        TrainTrip trainTrip = trainTripOpt.get();
        Route route = trainTrip.getRoute();

        List<Station> routeStations = route.getJourney();
        int boardingOrder = IntStream.range(0, routeStations.size())
                .filter(idx -> routeStations.get(idx).getStationId() == boardingStationId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ga lên tàu không thuộc lộ trình"));
        int alightingOrder = IntStream.range(0, routeStations.size())
                .filter(idx -> routeStations.get(idx).getStationId() == alightingStationId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ga xuống tàu không thuộc lộ trình"));

        if (boardingOrder >= alightingOrder) {
            throw new IllegalArgumentException("Ga lên tàu phải trước ga xuống tàu");
        }

        List<Seat> seats = seatService.findAvailableSeatsForSegment(trainTripId, boardingOrder, alightingOrder);
        return ResponseEntity.ok(seats);
    }
}