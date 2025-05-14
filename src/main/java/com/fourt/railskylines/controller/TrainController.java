package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Carriage;
import com.fourt.railskylines.domain.Train;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.service.TrainService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

// URL
@RequestMapping("/api/v1")
@CrossOrigin("https://railskylines-fe-1.onrender.com")
public class TrainController {
    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PostMapping("/trains")
    @APIMessage("Create a new train")
    public ResponseEntity<Train> createNewCompany(@Valid @RequestBody Train postmanTrain) throws IdInvalidException {
        boolean isTrainExist = this.trainService.isTrainExist(postmanTrain.getTrainName());
        if (isTrainExist) {
            throw new IdInvalidException("Train : " + postmanTrain.getTrainName() + " is exist , pls check again");
        }
        Train train = this.trainService.handleCreateTrain(postmanTrain);
        return ResponseEntity.status(HttpStatus.CREATED).body(train);
    }

    @GetMapping("/trains")
    @APIMessage("fetch all permissions")
    public ResponseEntity<ResultPaginationDTO> getAllTrains(
            @Filter Specification<Train> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.trainService.fetchAllTrains(spec, pageable));
    }

    @GetMapping("/trains/{id}")
    @APIMessage("Fetch train by ID")
    public ResponseEntity<Train> getTrainById(@PathVariable("id") Long id) throws IdInvalidException {
        if (this.trainService.fetchTrainById(id) == null) {
            throw new IdInvalidException("Train with id = " + id + " not exits , pls check again");
        }
        Train train = trainService.fetchTrainById(id);
        return ResponseEntity.ok().body(train);
    }

    @PutMapping("/trains/{id}")
    @APIMessage("update by train id")
    public ResponseEntity<Train> handleUpdateTrain(@PathVariable("id") Long id, @Valid @RequestBody Train train)
            throws IdInvalidException {
        if (this.trainService.fetchTrainById(id) == null) {
            throw new IdInvalidException("Train with id = not exits " + id + " , pls check again");
        }
        Train updateTrain = this.trainService.handleUpdateTrain(id, train);
        return ResponseEntity.ok(updateTrain);
    }

    @DeleteMapping("/trains/{id}")
    @APIMessage("Delete by train id")
    public ResponseEntity<String> updateCompany(@PathVariable("id") long id) throws IdInvalidException {
        if (this.trainService.fetchTrainById(id) == null) {
            throw new IdInvalidException("Id " + id + " isn't exits");
        }
        this.trainService.handleDeleteTrain(id);
        return ResponseEntity.ok("Delete Success");
    }

    @GetMapping("/trains/{id}/carriages")
    @APIMessage("Fetch carriages by Train ID")
    public ResponseEntity<List<Carriage>> getCarriagesByTrainId(@PathVariable("id") Long id) throws IdInvalidException {
        List<Carriage> carriages = this.trainService.fetchCarriagesByTrainId(id);
        return ResponseEntity.ok().body(carriages);
    }

}