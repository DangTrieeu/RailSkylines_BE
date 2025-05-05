package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
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

@RestController
@RequestMapping("/api/v1")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
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
}