package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fourt.RailSkylines.domain.Train;
import com.fourt.RailSkylines.domain.TrainTrip;

import java.time.Instant;
import java.util.List;

@Repository
public interface TrainTripRepository extends JpaRepository<TrainTrip, Long> {

        @Query("SELECT tt FROM TrainTrip tt " +
                        "JOIN tt.journey s1 " +
                        "JOIN tt.journey s2 " +
                        "WHERE s1.stationName = :departureStationName " +
                        "AND s2.stationName = :arrivalStationName " +
                        "AND s1.position < s2.position " +
                        "AND DATE(tt.departure) = DATE(:departureDate)")
        List<TrainTrip> findTrainTripsByStationNamesAndDate(
                        String departureStationName,
                        String arrivalStationName,
                        Instant departureDate);

        Optional<TrainTrip> findByTrain(Train train);

}