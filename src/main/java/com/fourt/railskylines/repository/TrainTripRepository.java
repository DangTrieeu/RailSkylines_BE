package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.TrainTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainTripRepository extends JpaRepository<TrainTrip, Long>, JpaSpecificationExecutor<TrainTrip> {
    boolean existsById(Long id);
}