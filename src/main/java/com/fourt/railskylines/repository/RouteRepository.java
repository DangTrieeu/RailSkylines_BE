package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Route;
import com.fourt.railskylines.domain.Station;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
    // @Query("SELECT r FROM Route r WHERE r.originStation = :originStation AND
    // r.journey = :journeyStations")
    // Optional<Route> findByOriginStationAndJourney(Station originStation,
    // List<Station> journeyStations);
    @Query("SELECT r FROM Route r JOIN r.journey j WHERE r.originStation = :originStation " +
            "GROUP BY r HAVING COUNT(j) = :journeySize AND SUM(CASE WHEN j IN :journeyStations THEN 1 ELSE 0 END) = :journeySize")
    Optional<Route> findByOriginStationAndJourney(@Param("originStation") Station originStation,
            @Param("journeyStations") List<Station> journeyStations,
            @Param("journeySize") long journeySize);

    List<Route> findByOriginStation(Station originStation);
}