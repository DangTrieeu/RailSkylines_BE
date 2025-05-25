package com.fourt.railskylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourt.railskylines.domain.Train;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long>,
        JpaSpecificationExecutor<Train> {
    boolean existsByTrainName(String trainName);
}
