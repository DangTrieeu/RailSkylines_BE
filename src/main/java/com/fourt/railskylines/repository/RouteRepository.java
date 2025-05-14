package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Route;
import com.fourt.railskylines.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
    @Query("SELECT r FROM Route r " +
           "WHERE r.originStation.stationName = :originStationName " +
           "AND EXISTS (" +
           "SELECT 1 FROM Route r2 JOIN r2.journey j " +
           "WHERE r2 = r " +
           "GROUP BY r2 " +
           "HAVING COUNT(j) = :journeySize " +
           "AND SUM(CASE WHEN j.stationName IN :journeyStationNames THEN 1 ELSE 0 END) = :journeySize" +
           ")")
    Optional<Route> findByOriginStationNameAndJourneyStationNames(
            @Param("originStationName") String originStationName,
            @Param("journeyStationNames") List<String> journeyStationNames,
            @Param("journeySize") long journeySize);

    List<Route> findByOriginStation(Station originStation);
}