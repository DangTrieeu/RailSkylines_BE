package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Carriage;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.Train;
import com.fourt.railskylines.domain.dto.ResultPaginationDTO;
import com.fourt.railskylines.service.CarriageService;
import com.fourt.railskylines.service.TrainService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;

import java.util.List;

import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CarriageController {

    private final TrainService trainService;

    private final CarriageService carriageService;

    public CarriageController(CarriageService carriageService, TrainService trainService) {
        this.carriageService = carriageService;
        this.trainService = trainService;
    }

    @PostMapping("/carriages")
    @APIMessage("Create a new carriage")
    public ResponseEntity<Carriage> createNewCarriage(@Valid @RequestBody Carriage carriage) {
        Carriage savedCarriage = this.carriageService.handleCreateCarriage(carriage);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCarriage);
    }

    @GetMapping("/carriages/seat/{id}")
    @APIMessage("Get seat by carriage id")
    public ResponseEntity<ResultPaginationDTO> fetchSeatByCarriage(
            @PathVariable("id") Long id,
            @Filter Specification<Seat> spec,
            Pageable pageable) throws IdInvalidException {
        Carriage carriage = this.carriageService.fetchCarriageById(id);
        if (carriage == null) {
            throw new IdInvalidException("Carriage with id = " + id + " does not exist,please check again");
        }
        return ResponseEntity.ok(this.carriageService.fetchAllSeatByCarriage(id, spec, pageable));
    }

    @GetMapping("/carriages")
    @APIMessage("Fetch all carriages")
    public ResponseEntity<ResultPaginationDTO> getAllCarriages(
            @Filter Specification<Carriage> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.carriageService.fetchAllCarriages(spec, pageable));
    }

    @GetMapping("/carriages/{id}")
    @APIMessage("Fetch carriage by ID")
    public ResponseEntity<Carriage> getCarriageById(@PathVariable("id") Long id) throws IdInvalidException {
        Carriage carriage = this.carriageService.fetchCarriageById(id);
        if (carriage == null) {
            throw new IdInvalidException("Carriage with id = " + id + " does not exist, please check again");
        }
        return ResponseEntity.ok().body(carriage);
    }

    @PutMapping("/carriages/{id}")
    @APIMessage("Update carriage by ID")
    public ResponseEntity<Carriage> handleUpdateCarriage(@PathVariable("id") Long id,
            @Valid @RequestBody Carriage carriage)
            throws IdInvalidException {
        Carriage existingCarriage = this.carriageService.fetchCarriageById(id);
        if (existingCarriage == null) {
            throw new IdInvalidException("Carriage with id = " + id + " does not exist, please check again");
        }
        Long newTrainId = carriage.getTrain() != null ? carriage.getTrain().getTrainId() : null;
        if (newTrainId == null || !this.trainService.existsById(newTrainId)) {
            throw new IdInvalidException("Train with id = " + newTrainId + " does not exist, please check again");
        }
        Carriage updatedCarriage = this.carriageService.handleUpdateCarriage(id, carriage);
        return ResponseEntity.ok(updatedCarriage);
    }

    @DeleteMapping("/carriages/{id}")
    @APIMessage("Delete carriage by ID")
    public ResponseEntity<String> deleteCarriage(@PathVariable("id") Long id) throws IdInvalidException {
        Carriage existingCarriage = this.carriageService.fetchCarriageById(id);
        if (existingCarriage == null) {
            throw new IdInvalidException("Carriage with id = " + id + " does not exist");
        }
        this.carriageService.handleDeleteCarriage(id);
        return ResponseEntity.ok("Delete Success");
    }
}