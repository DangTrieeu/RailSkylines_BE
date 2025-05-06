// package com.fourt.railskylines.controller;

// import com.fourt.railskylines.domain.TrainTrip;
// import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
// import com.fourt.railskylines.domain.response.ResultPaginationDTO;
// import com.fourt.railskylines.service.TrainTripService;
// import com.fourt.railskylines.util.annotation.APIMessage;
// import com.fourt.railskylines.util.error.IdInvalidException;
// import com.turkraft.springfilter.boot.Filter;
// import jakarta.validation.Valid;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/v1")
// public class TrainTripController {

//     private final TrainTripService trainTripService;

//     public TrainTripController(TrainTripService trainTripService) {
//         this.trainTripService = trainTripService;
//     }

//     @PostMapping("/train-trips")
//     @APIMessage("Create a new TrainTrip")
//     public ResponseEntity<TrainTrip> createNewTrainTrip(@Valid @RequestBody TrainTripRequestDTO request)
//             throws IdInvalidException {
//         TrainTrip trainTrip = this.trainTripService.handleCreateTrainTrip(request);
//         return ResponseEntity.status(HttpStatus.CREATED).body(trainTrip);
//     }

//     @GetMapping("/train-trips")
//     @APIMessage("Fetch all TrainTrips")
//     public ResponseEntity<ResultPaginationDTO> getAllTrainTrips(
//             @Filter Specification<TrainTrip> spec,
//             Pageable pageable) {
//         return ResponseEntity.status(HttpStatus.OK).body(this.trainTripService.fetchAllTrainTrips(spec, pageable));
//     }

//     @GetMapping("/train-trips/{id}")
//     @APIMessage("Fetch TrainTrip by ID")
//     public ResponseEntity<TrainTrip> getTrainTripById(@PathVariable("id") Long id) throws IdInvalidException {
//         TrainTrip trainTrip = this.trainTripService.fetchTrainTripById(id);
//         if (trainTrip == null) {
//             throw new IdInvalidException("TrainTrip with ID " + id + " does not exist, please check again");
//         }
//         return ResponseEntity.ok().body(trainTrip);
//     }

//     @PutMapping("/train-trips/{id}")
//     @APIMessage("Update TrainTrip by ID")
//     public ResponseEntity<TrainTrip> handleUpdateTrainTrip(
//             @PathVariable("id") Long id,
//             @Valid @RequestBody TrainTripRequestDTO request) throws IdInvalidException {
//         TrainTrip updatedTrainTrip = this.trainTripService.handleUpdateTrainTrip(id, request);
//         return ResponseEntity.ok(updatedTrainTrip);
//     }

//     @DeleteMapping("/train-trips/{id}")
//     @APIMessage("Delete TrainTrip by ID")
//     public ResponseEntity<String> deleteTrainTrip(@PathVariable("id") Long id) throws IdInvalidException {
//         this.trainTripService.handleDeleteTrainTrip(id);
//         return ResponseEntity.ok("Delete Success");
//     }
// }
package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.TrainTrip;
import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.domain.response.TrainTripResponseDTO;
import com.fourt.railskylines.service.TrainTripService;
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
public class TrainTripController {

    private final TrainTripService trainTripService;

    public TrainTripController(TrainTripService trainTripService) {
        this.trainTripService = trainTripService;
    }

    @PostMapping("/train-trips")
    @APIMessage("Create a new TrainTrip")
    public ResponseEntity<TrainTrip> createNewTrainTrip(@Valid @RequestBody TrainTripRequestDTO request)
            throws IdInvalidException {
        TrainTrip trainTrip = this.trainTripService.handleCreateTrainTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trainTrip);
    }

    @GetMapping("/train-trips")
    @APIMessage("Fetch all TrainTrips")
    public ResponseEntity<ResultPaginationDTO> getAllTrainTrips(
            @Filter Specification<TrainTrip> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.trainTripService.fetchAllTrainTrips(spec, pageable));
    }

    @GetMapping("/train-trips/{id}")
    @APIMessage("Fetch TrainTrip by ID")
    public ResponseEntity<TrainTripResponseDTO> getTrainTripById(@PathVariable("id") Long id)
            throws IdInvalidException {
        TrainTripResponseDTO trainTrip = this.trainTripService.fetchTrainTripById(id);
        if (trainTrip == null) {
            throw new IdInvalidException("TrainTrip with ID " + id + " does not exist, please check again");
        }
        return ResponseEntity.ok().body(trainTrip);
    }

    @PutMapping("/train-trips/{id}")
    @APIMessage("Update TrainTrip by ID")
    public ResponseEntity<TrainTrip> handleUpdateTrainTrip(
            @PathVariable("id") Long id,
            @Valid @RequestBody TrainTripRequestDTO request) throws IdInvalidException {
        TrainTrip updatedTrainTrip = this.trainTripService.handleUpdateTrainTrip(id, request);
        return ResponseEntity.ok(updatedTrainTrip);
    }

    @DeleteMapping("/train-trips/{id}")
    @APIMessage("Delete TrainTrip by ID")
    public ResponseEntity<String> deleteTrainTrip(@PathVariable("id") Long id) throws IdInvalidException {
        this.trainTripService.handleDeleteTrainTrip(id);
        return ResponseEntity.ok("Delete Success");
    }
}