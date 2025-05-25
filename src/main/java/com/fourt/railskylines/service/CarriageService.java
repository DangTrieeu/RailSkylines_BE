package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Carriage;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.repository.CarriageRepository;
import com.fourt.railskylines.repository.SeatRepository;
import com.fourt.railskylines.repository.TrainRepository;
import com.fourt.railskylines.util.constant.CarriageTypeEnum;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.SeatTypeEnum;
import com.fourt.railskylines.util.error.IdInvalidException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarriageService {

    private final CarriageRepository carriageRepository;
    private final TrainRepository trainRepository;
    private final SeatRepository seatRepository;

    public CarriageService(CarriageRepository carriageRepository, TrainRepository trainRepository,
            SeatRepository seatRepository) {
        this.carriageRepository = carriageRepository;
        this.trainRepository = trainRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public Carriage handleCreateCarriage(Carriage carriage) {
        // Validate train
        if (carriage.getTrain() == null || carriage.getTrain().getTrainId() == -1) {
            throw new IllegalArgumentException("Train ID must be provided");
        }

        // Check if train exists
        Long trainId = carriage.getTrain().getTrainId();
        if (!trainRepository.existsById(trainId)) {
            throw new IllegalArgumentException("Train with ID " + trainId + " does not exist");
        }

        // Validate carriage type
        if (carriage.getCarriageType() == null) {
            throw new IllegalArgumentException("Carriage type must be provided");
        }

        // Validate price
        if (carriage.getPrice() <= 0) {
            throw new IllegalArgumentException("Carriage price must be greater than zero");
        }

        // Validate discount (0 <= discount <= 0.5 to avoid negative prices for LEVEL_3)
        if (carriage.getDiscount() < 0 || carriage.getDiscount() > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        // Save carriage to generate ID
        Carriage savedCarriage = carriageRepository.save(carriage);

        return savedCarriage;
    }

    // Helper methods for seat type assignment

    public Carriage fetchCarriageById(long id) {
        Optional<Carriage> carriageOptional = this.carriageRepository.findById(id);
        return carriageOptional.orElse(null);
    }

    public ResultPaginationDTO fetchAllCarriages(Specification<Carriage> spec, Pageable pageable) {
        Page<Carriage> pageCarriage = this.carriageRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageCarriage.getTotalPages());
        mt.setTotal(pageCarriage.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageCarriage.getContent());
        return rs;
    }

    public Carriage handleUpdateCarriage(long id, Carriage carriage) {
        Carriage currentCarriage = this.fetchCarriageById(id);
        if (currentCarriage != null) {
            currentCarriage.setCarriageType(carriage.getCarriageType());
            currentCarriage.setTrain(carriage.getTrain());
            currentCarriage.setDiscount(carriage.getDiscount());
            currentCarriage.setPrice(carriage.getPrice());
            // Note: Seats are managed via a separate service or endpoint if needed
            this.carriageRepository.save(currentCarriage);
        }
        return currentCarriage;
    }

    public void handleDeleteCarriage(long id) {
        this.carriageRepository.deleteById(id);
    }

    public ResultPaginationDTO fetchAllSeatByCarriage(long id, Specification<Seat> spec, Pageable pageable)
            throws IdInvalidException {
        if (id <= 0) {
            throw new IdInvalidException("Carriage ID must be positive");
        }
        if (carriageRepository.findById(id).isEmpty()) {
            throw new IdInvalidException("Carriage with id = " + id + " does not exist");
        }
        Specification<Seat> finalSpec = Specification.where(carriageIdEqual(id))
                .and(spec != null ? spec : Specification.where(null));
        Page<Seat> pageSeat = this.seatRepository.findAll(finalSpec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageSeat.getTotalPages());
        mt.setTotal(pageSeat.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageSeat.getContent());
        return rs;
    }

    private Specification<Seat> carriageIdEqual(long id) {
        return (root, query, cb) -> cb.equal(root.get("carriage").get("carriageId"), id);
    }

}