package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Carriage;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.TrainTrip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.data.domain.Page;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, JpaSpecificationExecutor<Seat> {
    Page<Seat> findByCarriage_CarriageId(Long carriageId, Pageable pageable);

    List<Seat> findByCarriageIn(List<Carriage> carriages);

    List<Seat> findByTrainTrip(TrainTrip trainTrip);
}