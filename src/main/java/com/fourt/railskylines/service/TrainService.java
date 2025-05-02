package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Train;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.repository.TrainRepository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TrainService {
    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
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

    public void handleDeleteTrain(long id) {

        this.trainRepository.deleteById(id);
    }

    public boolean isTrainExist(String trainName) {
        return this.trainRepository.existsByTrainName(trainName);
    }

    public boolean existsById(long id) {
        return this.trainRepository.existsById(id);
    }
}