package com.fourt.railskylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourt.railskylines.domain.Station;

@Repository
public interface StationRepository extends JpaRepository<Station, Long>, JpaSpecificationExecutor<Station> {
    boolean existsByStationName(String stationName);

    Station findByStationName(String stationName);
}