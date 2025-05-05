package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Carriage;
import com.fourt.railskylines.domain.Train;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.repository.CarriageRepository;
import com.fourt.railskylines.repository.TrainRepository;
import com.fourt.railskylines.util.error.IdInvalidException;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TrainService {

    private final CarriageRepository carriageRepository;
    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository, CarriageRepository carriageRepository) {
        this.trainRepository = trainRepository;
        this.carriageRepository = carriageRepository;
    }

    public Train handleCreateTrain(Train train) {

        return trainRepository.save(train);
    }

    public Train fetchTrainById(long id) {
        Optional<Train> trainOptional = this.trainRepository.findById(id);
        if (trainOptional.isPresent()) {
            return trainOptional.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllTrains(Specification<Train> spec, Pageable pageable) {
        Page<Train> pageTrain = this.trainRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageTrain.getTotalPages());
        mt.setTotal(pageTrain.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageTrain.getContent());
        return rs;
    }

    public Train handleUpdateTrain(long id, Train train) {
        Train currentTrain = this.fetchTrainById(id);
        if (currentTrain != null) {
            currentTrain.setTrainName(train.getTrainName());
            currentTrain.setTrainStatus(train.getTrainStatus());
            // updateTrain.setTrip(train.getTrip());
            // updateTrain.setCarriages(train.getCarriages());
            this.trainRepository.save(currentTrain);

        }
        return currentTrain;
    }

    @Transactional
    public void handleDeleteTrain(long id) {
        this.carriageRepository.deleteByTrain_TrainId(id);
        // Then delete the train
        this.trainRepository.deleteById(id);
    }

    public boolean isTrainExist(String trainName) {
        return this.trainRepository.existsByTrainName(trainName);
    }

    public boolean existsById(long id) {
        return this.trainRepository.existsById(id);
    }

    public List<Carriage> fetchCarriagesByTrainId(long trainId) throws IdInvalidException {
        if (!trainRepository.existsById(trainId)) {
            throw new IdInvalidException("Train with ID " + trainId + " does not exist");
        }
        return this.carriageRepository.findByTrainTrainId(trainId);
    }
}