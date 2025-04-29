package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fourt.RailSkylines.domain.Station;

public interface StationRepository extends JpaRepository<Station, Long> {
    Station findByStationName(String stationName);
}
