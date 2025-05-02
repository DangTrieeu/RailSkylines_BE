package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Station;
import com.fourt.railskylines.domain.dto.ResultPaginationDTO;
import com.fourt.railskylines.service.StationService;
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
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping("/stations")
    @APIMessage("Create a new station")
    public ResponseEntity<Station> createNewStation(@Valid @RequestBody Station station) throws IdInvalidException {
        boolean isStationExist = this.stationService.isStationExist(station.getStationName());
        if (isStationExist) {
            throw new IdInvalidException("Station : " + station.getStationName() + " is exist , pls check again");
        }
        Station savedStation = stationService.handleCreateStation(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedStation);
    }

    @GetMapping("/stations")
    @APIMessage("Fetch all stations")
    public ResponseEntity<ResultPaginationDTO> getAllStations(
            @Filter Specification<Station> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(stationService.fetchAllStations(spec, pageable));
    }

    @GetMapping("/stations/{id}")
    @APIMessage("Fetch station by ID")
    public ResponseEntity<Station> getStationById(@PathVariable("id") Long id) throws IdInvalidException {
        Station station = stationService.fetchStationById(id);
        if (station == null) {
            throw new IdInvalidException("Station with id = " + id + " does not exist, please check again");
        }
        return ResponseEntity.ok().body(station);
    }

    // @PutMapping("/stations/{id}")
    // @APIMessage("Update station by ID")
    // public ResponseEntity<Station> handleUpdateStation(
    // @PathVariable("id") Long id,
    // @Valid @RequestBody Station station) throws IdInvalidException {
    // boolean isStationExist =
    // this.stationService.isStationExist(station.getStationName());

    // Station stationDb = stationService.fetchStationById(id);
    // if (isStationExist) {
    // throw new IdInvalidException("Station : " + station.getStationName() + " is
    // exist , pls check again");
    // }
    // Station updatedStation = stationService.handleUpdateStation(id, station);
    // return ResponseEntity.ok(updatedStation);
    // }

    @PutMapping("/stations/{id}")
    @APIMessage("Update station by ID")
    public ResponseEntity<Station> handleUpdateStation(
            @PathVariable("id") Long id,
            @Valid @RequestBody Station station) throws IdInvalidException {

        Station stationDb = stationService.fetchStationById(id);

        // Nếu tên station mới khác với tên cũ, thì kiểm tra trùng
        if (!station.getStationName().equals(stationDb.getStationName())) {
            boolean isStationExist = stationService.isStationExist(station.getStationName());
            if (isStationExist) {
                throw new IdInvalidException(
                        "Station: " + station.getStationName() + " is already exist, please check again");
            }
        }

        Station updatedStation = stationService.handleUpdateStation(id, station);
        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping("/stations/{id}")
    @APIMessage("Delete station by ID")
    public ResponseEntity<String> deleteStation(@PathVariable("id") Long id) throws IdInvalidException {
        stationService.handleDeleteStation(id);
        return ResponseEntity.ok("Delete Success");
    }
}