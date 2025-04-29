package com.fourt.RailSkylines.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fourt.RailSkylines.domain.Station;
import com.fourt.RailSkylines.domain.Train;
import com.fourt.RailSkylines.domain.TrainTrip;
import com.fourt.RailSkylines.repository.StationRepository;
import com.fourt.RailSkylines.repository.TrainRepository;
import com.fourt.RailSkylines.repository.TrainTripRepository;
import com.fourt.RailSkylines.util.constant.TrainStatusEnum;

@Service
public class TrainService {
        private TrainRepository trainRepository;
        private TrainTripRepository trainTripRepository;
        private StationRepository stationRepository;

        public TrainService(TrainRepository trainRepository,
                        TrainTripRepository trainTripRepository,
                        StationRepository stationRepository) {
                this.trainRepository = trainRepository;
                this.trainTripRepository = trainTripRepository;
                this.stationRepository = stationRepository;
        }

        public List<Train> findTrainsByStationNamesAndDate(String departureStationName, String arrivalStationName,
                        Instant departureDate) {
                // Kiểm tra xem ga có tồn tại không
                Station departureStation = stationRepository.findByStationName(departureStationName);
                Station arrivalStation = stationRepository.findByStationName(arrivalStationName);

                if (departureStation == null || arrivalStation == null) {
                        throw new IllegalArgumentException("Ga không tồn tại: " +
                                        (departureStation == null ? departureStationName : arrivalStationName));
                }

                // Tìm các TrainTrip phù hợp
                List<TrainTrip> trainTrips = trainTripRepository.findTrainTripsByStationNamesAndDate(
                                departureStationName, arrivalStationName, departureDate);

                // Chuyển đổi từ TrainTrip sang Train
                return trainTrips.stream()
                                .map(TrainTrip::getTrain)
                                .filter(train -> train != null &&
                                                train.getTrainStatus() == TrainStatusEnum.active) // Lọc tàu đang hoạt
                                                                                                  // động
                                .collect(Collectors.toList());
        }
}
