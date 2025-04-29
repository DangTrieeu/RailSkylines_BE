package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fourt.RailSkylines.domain.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

}
