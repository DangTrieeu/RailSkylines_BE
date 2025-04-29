package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourt.RailSkylines.domain.Train;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

}
