package com.fourt.railskylines.service;

// import com.fourt.railskylines.domain.*;
// import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
// import com.fourt.railskylines.repository.*;
// import com.fourt.railskylines.domain.response.ResultPaginationDTO;
// import com.fourt.railskylines.util.constant.SeatStatusEnum;
// import com.fourt.railskylines.util.constant.SeatTypeEnum;
// import com.fourt.railskylines.util.error.IdInvalidException;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.ZoneId;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// public class TrainTripService {

//     private final TrainTripRepository trainTripRepository;
//     private final TrainRepository trainRepository;
//     private final StationRepository stationRepository;
//     private final RouteRepository routeRepository;
//     private final ScheduleRepository scheduleRepository;
//     private final ClockTimeRepository clockTimeRepository;
//     private final CarriageRepository carriageRepository;
//     private final SeatRepository seatRepository;

//     public TrainTripService(
//             TrainTripRepository trainTripRepository,
//             TrainRepository trainRepository,
//             StationRepository stationRepository,
//             RouteRepository routeRepository,
//             ScheduleRepository scheduleRepository,
//             ClockTimeRepository clockTimeRepository,
//             CarriageRepository carriageRepository,
//             SeatRepository seatRepository) {
//         this.trainTripRepository = trainTripRepository;
//         this.trainRepository = trainRepository;
//         this.stationRepository = stationRepository;
//         this.routeRepository = routeRepository;
//         this.scheduleRepository = scheduleRepository;
//         this.clockTimeRepository = clockTimeRepository;
//         this.carriageRepository = carriageRepository;
//         this.seatRepository = seatRepository;
//     }

//     @Transactional
//     public TrainTrip handleCreateTrainTrip(TrainTripRequestDTO request) throws IdInvalidException {
//         // 1. Validate Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();

//         // 2. Tạo hoặc tìm Station cho originStation
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         // 3. Tạo hoặc tìm Station cho journey
//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         // 4. Tìm hoặc tạo Route
//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = new Route();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }

//         // 5. Tạo ClockTime
//         ClockTime departure = new ClockTime();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = new ClockTime();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         // 6. Tạo Schedule
//         Schedule schedule = new Schedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);

//         // 7. Tạo TrainTrip
//         TrainTrip trainTrip = new TrainTrip();
//         trainTrip.setTrain(train);
//         trainTrip.setRoute(route);
//         trainTrip.setSchedule(schedule);
//         trainTrip = trainTripRepository.save(trainTrip);

//         // 8. Tạo Seats cho TrainTrip dựa trên Carriages của Train
//         List<Carriage> carriages = carriageRepository.findByTrain(train);
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(trainTrip); // Liên kết Seat với TrainTrip

//                 // Gán SeatType và Price dựa trên CarriageService logic
//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTrip;
//     }

//     public TrainTrip fetchTrainTripById(Long id) {
//         Optional<TrainTrip> trainTripOptional = trainTripRepository.findById(id);
//         return trainTripOptional.orElse(null);
//     }

//     public ResultPaginationDTO fetchAllTrainTrips(Specification<TrainTrip> spec, Pageable pageable) {
//         Page<TrainTrip> pageTrainTrip = trainTripRepository.findAll(spec, pageable);
//         ResultPaginationDTO rs = new ResultPaginationDTO();
//         ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
//         mt.setPage(pageable.getPageNumber() + 1);
//         mt.setPageSize(pageable.getPageSize());
//         mt.setPages(pageTrainTrip.getTotalPages());
//         mt.setTotal(pageTrainTrip.getTotalElements());
//         rs.setMeta(mt);
//         rs.setResult(pageTrainTrip.getContent());
//         return rs;
//     }

//     @Transactional
//     public TrainTrip handleUpdateTrainTrip(Long id, TrainTripRequestDTO request) throws IdInvalidException {
//         TrainTrip existingTrainTrip = fetchTrainTripById(id);
//         if (existingTrainTrip == null) {
//             throw new IdInvalidException("TrainTrip with ID " + id + " does not exist");
//         }

//         // Cập nhật Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();
//         existingTrainTrip.setTrain(train);

//         // Cập nhật Route
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = existingTrainTrip.getRoute();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }
//         existingTrainTrip.setRoute(route);

//         // Cập nhật Schedule
//         ClockTime departure = existingTrainTrip.getSchedule().getDeparture();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = existingTrainTrip.getSchedule().getArrival();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         Schedule schedule = existingTrainTrip.getSchedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);
//         existingTrainTrip.setSchedule(schedule);

//         // Cập nhật Seats (xóa seats cũ và tạo mới dựa trên Carriages của Train)
//         List<Carriage> carriages = carriageRepository.findByTrain(train);
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         List<Seat> existingSeats = this.seatRepository.findByTrainTrip(existingTrainTrip);
//         seatRepository.deleteAll(existingSeats);

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(existingTrainTrip); // Liên kết Seat với TrainTrip

//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTripRepository.save(existingTrainTrip);
//     }

//     public void handleDeleteTrainTrip(Long id) throws IdInvalidException {
//         if (!trainTripRepository.existsById(id)) {
//             throw new IdInvalidException("TrainTrip with ID " + id + " does not exist");
//         }
//         trainTripRepository.deleteById(id);
//     }

//     // Helper methods từ CarriageService
//     private boolean isSixBedsLevel3(int seatNumber) {
//         return (seatNumber % 6 == 1 || seatNumber % 6 == 2);
//     }

//     private boolean isSixBedsLevel2(int seatNumber) {
//         return (seatNumber % 6 == 3 || seatNumber % 6 == 4);
//     }

//     private boolean isFourBedsLevel1(int seatNumber) {
//         return (seatNumber % 4 == 3 || seatNumber % 4 == 0);
//     }
// }

// import com.fourt.railskylines.domain.*;
// import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
// import com.fourt.railskylines.domain.response.ResultPaginationDTO;
// import com.fourt.railskylines.domain.response.TrainTripResponseDTO;
// import com.fourt.railskylines.repository.*;
// import com.fourt.railskylines.util.constant.SeatStatusEnum;
// import com.fourt.railskylines.util.constant.SeatTypeEnum;
// import com.fourt.railskylines.util.error.IdInvalidException;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.ZoneId;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// public class TrainTripService {

//     private final TrainTripRepository trainTripRepository;
//     private final TrainRepository trainRepository;
//     private final StationRepository stationRepository;
//     private final RouteRepository routeRepository;
//     private final ScheduleRepository scheduleRepository;
//     private final ClockTimeRepository clockTimeRepository;
//     private final CarriageRepository carriageRepository;
//     private final SeatRepository seatRepository;

//     public TrainTripService(
//             TrainTripRepository trainTripRepository,
//             TrainRepository trainRepository,
//             StationRepository stationRepository,
//             RouteRepository routeRepository,
//             ScheduleRepository scheduleRepository,
//             ClockTimeRepository clockTimeRepository,
//             CarriageRepository carriageRepository,
//             SeatRepository seatRepository) {
//         this.trainTripRepository = trainTripRepository;
//         this.trainRepository = trainRepository;
//         this.stationRepository = stationRepository;
//         this.routeRepository = routeRepository;
//         this.scheduleRepository = scheduleRepository;
//         this.clockTimeRepository = clockTimeRepository;
//         this.carriageRepository = carriageRepository;
//         this.seatRepository = seatRepository;
//     }

//     @Transactional
//     public TrainTrip handleCreateTrainTrip(TrainTripRequestDTO request) throws IdInvalidException {
//         // 1. Validate Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();

//         // 2. Tạo hoặc tìm Station cho originStation
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         // 3. Tạo hoặc tìm Station cho journey
//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         // 4. Tìm hoặc tạo Route
//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = new Route();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }

//         // 5. Tạo ClockTime
//         ClockTime departure = new ClockTime();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = new ClockTime();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         // 6. Tạo Schedule
//         Schedule schedule = new Schedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);

//         // 7. Tạo TrainTrip
//         TrainTrip trainTrip = new TrainTrip();
//         trainTrip.setTrain(train);
//         trainTrip.setRoute(route);
//         trainTrip.setSchedule(schedule);
//         trainTrip = trainTripRepository.save(trainTrip);

//         // 8. Tạo Seats cho TrainTrip dựa trên Carriages của Train
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(trainTrip); // Liên kết Seat với TrainTrip

//                 // Gán SeatType và Price dựa trên CarriageService logic
//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTrip;
//     }

//     public TrainTripResponseDTO fetchTrainTripById(Long id) {
//         Optional<TrainTrip> trainTripOptional = trainTripRepository.findById(id);
//         if (trainTripOptional.isEmpty()) {
//             return null;
//         }

//         TrainTrip trainTrip = trainTripOptional.get();
//         TrainTripResponseDTO response = new TrainTripResponseDTO();
//         response.setTrainTripId(trainTrip.getTrainTripId());

//         // Map Train and Carriages
//         TrainTripResponseDTO.TrainDTO trainDTO = new TrainTripResponseDTO.TrainDTO();
//         trainDTO.setTrainId(trainTrip.getTrain().getTrainId());
//         trainDTO.setTrainName(trainTrip.getTrain().getTrainName());
//         trainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

//         // Fetch Carriages
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
//         List<TrainTripResponseDTO.CarriageDTO> carriageDTOs = carriages.stream().map(carriage -> {
//             TrainTripResponseDTO.CarriageDTO carriageDTO = new TrainTripResponseDTO.CarriageDTO();
//             carriageDTO.setCarriageId(carriage.getCarriageId());
//             carriageDTO.setCarriageType(carriage.getCarriageType().toString());
//             carriageDTO.setPrice(carriage.getPrice());
//             carriageDTO.setDiscount(carriage.getDiscount());
//             carriageDTO.setTrain(trainDTO);
//             return carriageDTO;
//         }).collect(Collectors.toList());

//         trainDTO.setCarriages(carriageDTOs);
//         response.setTrain(trainDTO);
//         response.setRoute(trainTrip.getRoute());
//         response.setSchedule(trainTrip.getSchedule());

//         return response;
//     }

//     public ResultPaginationDTO fetchAllTrainTrips(Specification<TrainTrip> spec, Pageable pageable) {
//         Page<TrainTrip> pageTrainTrip = trainTripRepository.findAll(spec, pageable);
//         ResultPaginationDTO rs = new ResultPaginationDTO();
//         ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
//         mt.setPage(pageable.getPageNumber() + 1);
//         mt.setPageSize(pageable.getPageSize());
//         mt.setPages(pageTrainTrip.getTotalPages());
//         mt.setTotal(pageTrainTrip.getTotalElements());
//         rs.setMeta(mt);
//         rs.setResult(pageTrainTrip.getContent());
//         return rs;
//     }

//     @Transactional
//     public TrainTrip handleUpdateTrainTrip(Long id, TrainTripRequestDTO request) throws IdInvalidException {
//         TrainTrip existingTrainTrip = trainTripRepository.findById(id)
//                 .orElseThrow(() -> new IdInvalidException("TrainTrip with ID " + id + " does not exist"));

//         // Cập nhật Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();
//         existingTrainTrip.setTrain(train);

//         // Cập nhật Route
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = existingTrainTrip.getRoute();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }
//         existingTrainTrip.setRoute(route);

//         // Cập nhật Schedule
//         ClockTime departure = existingTrainTrip.getSchedule().getDeparture();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = existingTrainTrip.getSchedule().getArrival();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         Schedule schedule = existingTrainTrip.getSchedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);
//         existingTrainTrip.setSchedule(schedule);

//         // Cập nhật Seats (xóa seats cũ và tạo mới dựa trên Carriages của Train)
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         List<Seat> existingSeats = this.seatRepository.findByTrainTrip(existingTrainTrip);
//         seatRepository.deleteAll(existingSeats);

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(existingTrainTrip); // Liên kết Seat với TrainTrip

//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTripRepository.save(existingTrainTrip);
//     }

//     public void handleDeleteTrainTrip(Long id) throws IdInvalidException {
//         if (!trainTripRepository.existsById(id)) {
//             throw new IdInvalidException("TrainTrip with ID " + id + " does not exist");
//         }
//         trainTripRepository.deleteById(id);
//     }

//     // Helper methods từ CarriageService
//     private boolean isSixBedsLevel3(int seatNumber) {
//         return (seatNumber % 6 == 1 || seatNumber % 6 == 2);
//     }

//     private boolean isSixBedsLevel2(int seatNumber) {
//         return (seatNumber % 6 == 3 || seatNumber % 6 == 4);
//     }

//     private boolean isFourBedsLevel1(int seatNumber) {
//         return (seatNumber % 4 == 3 || seatNumber % 4 == 0);
//     }
// }

// import com.fourt.railskylines.domain.*;
// import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
// import com.fourt.railskylines.domain.response.ResultPaginationDTO;
// import com.fourt.railskylines.domain.response.TrainTripResponseDTO;
// import com.fourt.railskylines.repository.*;
// import com.fourt.railskylines.util.constant.SeatStatusEnum;
// import com.fourt.railskylines.util.constant.SeatTypeEnum;
// import com.fourt.railskylines.util.error.IdInvalidException;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.ZoneId;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// public class TrainTripService {

//     private final TrainTripRepository trainTripRepository;
//     private final TrainRepository trainRepository;
//     private final StationRepository stationRepository;
//     private final RouteRepository routeRepository;
//     private final ScheduleRepository scheduleRepository;
//     private final ClockTimeRepository clockTimeRepository;
//     private final CarriageRepository carriageRepository;
//     private final SeatRepository seatRepository;

//     public TrainTripService(
//             TrainTripRepository trainTripRepository,
//             TrainRepository trainRepository,
//             StationRepository stationRepository,
//             RouteRepository routeRepository,
//             ScheduleRepository scheduleRepository,
//             ClockTimeRepository clockTimeRepository,
//             CarriageRepository carriageRepository,
//             SeatRepository seatRepository) {
//         this.trainTripRepository = trainTripRepository;
//         this.trainRepository = trainRepository;
//         this.stationRepository = stationRepository;
//         this.routeRepository = routeRepository;
//         this.scheduleRepository = scheduleRepository;
//         this.clockTimeRepository = clockTimeRepository;
//         this.carriageRepository = carriageRepository;
//         this.seatRepository = seatRepository;
//     }

//     @Transactional
//     public TrainTrip handleCreateTrainTrip(TrainTripRequestDTO request) throws IdInvalidException {
//         // 1. Validate Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();

//         // 2. Tạo hoặc tìm Station cho originStation
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         // 3. Tạo hoặc tìm Station cho journey
//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         // 4. Tìm hoặc tạo Route
//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = new Route();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }

//         // 5. Tạo ClockTime
//         ClockTime departure = new ClockTime();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = new ClockTime();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         // 6. Tạo Schedule
//         Schedule schedule = new Schedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);

//         // 7. Tạo TrainTrip
//         TrainTrip trainTrip = new TrainTrip();
//         trainTrip.setTrain(train);
//         trainTrip.setRoute(route);
//         trainTrip.setSchedule(schedule);
//         trainTrip = trainTripRepository.save(trainTrip);

//         // 8. Tạo Seats cho TrainTrip dựa trên Carriages của Train
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(trainTrip); // Liên kết Seat với TrainTrip

//                 // Gán SeatType và Price dựa trên CarriageService logic
//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTrip;
//     }

//     public TrainTripResponseDTO fetchTrainTripById(Long id) {
//         Optional<TrainTrip> trainTripOptional = trainTripRepository.findById(id);
//         if (trainTripOptional.isEmpty()) {
//             return null;
//         }

//         TrainTrip trainTrip = trainTripOptional.get();
//         TrainTripResponseDTO response = new TrainTripResponseDTO();
//         response.setTrainTripId(trainTrip.getTrainTripId());

//         // Map Train and Carriages
//         TrainTripResponseDTO.TrainDTO trainDTO = new TrainTripResponseDTO.TrainDTO();
//         trainDTO.setTrainId(trainTrip.getTrain().getTrainId());
//         trainDTO.setTrainName(trainTrip.getTrain().getTrainName());
//         trainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

//         // Create SimpleTrainDTO for CarriageDTO to avoid recursion
//         TrainTripResponseDTO.SimpleTrainDTO simpleTrainDTO = new TrainTripResponseDTO.SimpleTrainDTO();
//         simpleTrainDTO.setTrainId(trainTrip.getTrain().getTrainId());
//         simpleTrainDTO.setTrainName(trainTrip.getTrain().getTrainName());
//         simpleTrainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

//         // Fetch Carriages
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
//         List<TrainTripResponseDTO.CarriageDTO> carriageDTOs = carriages.stream().map(carriage -> {
//             TrainTripResponseDTO.CarriageDTO carriageDTO = new TrainTripResponseDTO.CarriageDTO();
//             carriageDTO.setCarriageId(carriage.getCarriageId());
//             carriageDTO.setCarriageType(carriage.getCarriageType().toString());
//             carriageDTO.setPrice(carriage.getPrice());
//             carriageDTO.setDiscount(carriage.getDiscount());
//             carriageDTO.setTrain(simpleTrainDTO);
//             return carriageDTO;
//         }).collect(Collectors.toList());

//         trainDTO.setCarriages(carriageDTOs);
//         response.setTrain(trainDTO);
//         response.setRoute(trainTrip.getRoute());
//         response.setSchedule(trainTrip.getSchedule());

//         return response;
//     }

//     public ResultPaginationDTO fetchAllTrainTrips(Specification<TrainTrip> spec, Pageable pageable) {
//         Page<TrainTrip> pageTrainTrip = trainTripRepository.findAll(spec, pageable);
//         ResultPaginationDTO rs = new ResultPaginationDTO();
//         ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
//         mt.setPage(pageable.getPageNumber() + 1);
//         mt.setPageSize(pageable.getPageSize());
//         mt.setPages(pageTrainTrip.getTotalPages());
//         mt.setTotal(pageTrainTrip.getTotalElements());
//         rs.setMeta(mt);
//         rs.setResult(pageTrainTrip.getContent());
//         return rs;
//     }

//     @Transactional
//     public TrainTrip handleUpdateTrainTrip(Long id, TrainTripRequestDTO request) throws IdInvalidException {
//         TrainTrip existingTrainTrip = trainTripRepository.findById(id)
//                 .orElseThrow(() -> new IdInvalidException("TrainTrip with ID " + id + " does not exist"));

//         // Cập nhật Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();
//         existingTrainTrip.setTrain(train);

//         // Cập nhật Route
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = existingTrainTrip.getRoute();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }
//         existingTrainTrip.setRoute(route);

//         // Cập nhật Schedule
//         ClockTime departure = existingTrainTrip.getSchedule().getDeparture();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = existingTrainTrip.getSchedule().getArrival();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         Schedule schedule = existingTrainTrip.getSchedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);
//         existingTrainTrip.setSchedule(schedule);

//         // Cập nhật Seats (xóa seats cũ và tạo mới dựa trên Carriages của Train)
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         List<Seat> existingSeats = this.seatRepository.findByTrainTrip(existingTrainTrip);
//         seatRepository.deleteAll(existingSeats);

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(existingTrainTrip); // Liên kết Seat với TrainTrip

//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTripRepository.save(existingTrainTrip);
//     }

//     public void handleDeleteTrainTrip(Long id) throws IdInvalidException {
//         if (!trainTripRepository.existsById(id)) {
//             throw new IdInvalidException("TrainTrip with ID " + id + " does not exist");
//         }
//         trainTripRepository.deleteById(id);
//     }

//     // Helper methods từ CarriageService
//     private boolean isSixBedsLevel3(int seatNumber) {
//         return (seatNumber % 6 == 1 || seatNumber % 6 == 2);
//     }

//     private boolean isSixBedsLevel2(int seatNumber) {
//         return (seatNumber % 6 == 3 || seatNumber % 6 == 4);
//     }

//     private boolean isFourBedsLevel1(int seatNumber) {
//         return (seatNumber % 4 == 3 || seatNumber % 4 == 0);
//     }
// }

/////////////////////////////

// import com.fourt.railskylines.domain.*;
// import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
// import com.fourt.railskylines.domain.response.ResultPaginationDTO;
// import com.fourt.railskylines.domain.response.TrainTripResponseDTO;
// import com.fourt.railskylines.repository.*;
// import com.fourt.railskylines.util.constant.SeatStatusEnum;
// import com.fourt.railskylines.util.constant.SeatTypeEnum;
// import com.fourt.railskylines.util.error.IdInvalidException;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.ZoneId;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// public class TrainTripService {

//     private final TrainTripRepository trainTripRepository;
//     private final TrainRepository trainRepository;
//     private final StationRepository stationRepository;
//     private final RouteRepository routeRepository;
//     private final ScheduleRepository scheduleRepository;
//     private final ClockTimeRepository clockTimeRepository;
//     private final CarriageRepository carriageRepository;
//     private final SeatRepository seatRepository;

//     public TrainTripService(
//             TrainTripRepository trainTripRepository,
//             TrainRepository trainRepository,
//             StationRepository stationRepository,
//             RouteRepository routeRepository,
//             ScheduleRepository scheduleRepository,
//             ClockTimeRepository clockTimeRepository,
//             CarriageRepository carriageRepository,
//             SeatRepository seatRepository) {
//         this.trainTripRepository = trainTripRepository;
//         this.trainRepository = trainRepository;
//         this.stationRepository = stationRepository;
//         this.routeRepository = routeRepository;
//         this.scheduleRepository = scheduleRepository;
//         this.clockTimeRepository = clockTimeRepository;
//         this.carriageRepository = carriageRepository;
//         this.seatRepository = seatRepository;
//     }

//     @Transactional
//     public TrainTrip handleCreateTrainTrip(TrainTripRequestDTO request) throws IdInvalidException {
//         // 1. Validate Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();

//         // 2. Tạo hoặc tìm Station cho originStation
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         // 3. Tạo hoặc tìm Station cho journey
//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         // 4. Tìm hoặc tạo Route
//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = new Route();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }

//         // 5. Tạo ClockTime
//         ClockTime departure = new ClockTime();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = new ClockTime();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         // 6. Tạo Schedule
//         Schedule schedule = new Schedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);

//         // 7. Tạo TrainTrip
//         TrainTrip trainTrip = new TrainTrip();
//         trainTrip.setTrain(train);
//         trainTrip.setRoute(route);
//         trainTrip.setSchedule(schedule);
//         trainTrip = trainTripRepository.save(trainTrip);

//         // 8. Tạo Seats cho TrainTrip dựa trên Carriages của Train
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(trainTrip); // Liên kết Seat với TrainTrip

//                 // Gán SeatType và Price dựa trên CarriageService logic
//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTrip;
//     }

//     public TrainTripResponseDTO fetchTrainTripById(Long id) {
//         Optional<TrainTrip> trainTripOptional = trainTripRepository.findById(id);
//         if (trainTripOptional.isEmpty()) {
//             return null;
//         }

//         TrainTrip trainTrip = trainTripOptional.get();
//         TrainTripResponseDTO response = new TrainTripResponseDTO();
//         response.setTrainTripId(trainTrip.getTrainTripId());

//         // Map Train and Carriages
//         TrainTripResponseDTO.TrainDTO trainDTO = new TrainTripResponseDTO.TrainDTO();
//         trainDTO.setTrainId(trainTrip.getTrain().getTrainId());
//         trainDTO.setTrainName(trainTrip.getTrain().getTrainName());
//         trainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

//         // Create SimpleTrainDTO for CarriageDTO to avoid recursion
//         TrainTripResponseDTO.SimpleTrainDTO simpleTrainDTO = new TrainTripResponseDTO.SimpleTrainDTO();
//         simpleTrainDTO.setTrainId(trainTrip.getTrain().getTrainId());
//         simpleTrainDTO.setTrainName(trainTrip.getTrain().getTrainName());
//         simpleTrainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

//         // Fetch Carriages
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
//         List<TrainTripResponseDTO.CarriageDTO> carriageDTOs = carriages.stream().map(carriage -> {
//             TrainTripResponseDTO.CarriageDTO carriageDTO = new TrainTripResponseDTO.CarriageDTO();
//             carriageDTO.setCarriageId(carriage.getCarriageId());
//             carriageDTO.setCarriageType(carriage.getCarriageType().toString());
//             carriageDTO.setPrice(carriage.getPrice());
//             carriageDTO.setDiscount(carriage.getDiscount());
//             carriageDTO.setTrain(simpleTrainDTO);

//             // Fetch Seats for this Carriage
//             List<Seat> seats = seatRepository.findByCarriageCarriageIdAndTrainTripTrainTripId(carriage.getCarriageId(),
//                     trainTrip.getTrainTripId());
//             List<TrainTripResponseDTO.SeatDTO> seatDTOs = seats.stream().map(seat -> {
//                 TrainTripResponseDTO.SeatDTO seatDTO = new TrainTripResponseDTO.SeatDTO();
//                 seatDTO.setSeatId(seat.getSeatId());
//                 seatDTO.setSeatType(seat.getSeatType());
//                 seatDTO.setSeatStatus(seat.getSeatStatus());
//                 seatDTO.setPrice(seat.getPrice());
//                 return seatDTO;
//             }).collect(Collectors.toList());

//             carriageDTO.setSeats(seatDTOs);
//             return carriageDTO;
//         }).collect(Collectors.toList());

//         trainDTO.setCarriages(carriageDTOs);
//         response.setTrain(trainDTO);
//         response.setRoute(trainTrip.getRoute());
//         response.setSchedule(trainTrip.getSchedule());

//         return response;

//     }

//     public ResultPaginationDTO fetchAllTrainTrips(Specification<TrainTrip> spec, Pageable pageable) {
//         Page<TrainTrip> pageTrainTrip = trainTripRepository.findAll(spec, pageable);
//         ResultPaginationDTO rs = new ResultPaginationDTO();
//         ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
//         mt.setPage(pageable.getPageNumber() + 1);
//         mt.setPageSize(pageable.getPageSize());
//         mt.setPages(pageTrainTrip.getTotalPages());
//         mt.setTotal(pageTrainTrip.getTotalElements());
//         rs.setMeta(mt);
//         rs.setResult(pageTrainTrip.getContent());
//         return rs;
//     }

//     @Transactional
//     public TrainTrip handleUpdateTrainTrip(Long id, TrainTripRequestDTO request) throws IdInvalidException {
//         TrainTrip existingTrainTrip = trainTripRepository.findById(id)
//                 .orElseThrow(() -> new IdInvalidException("TrainTrip with ID " + id + " does not exist"));

//         // Cập nhật Train
//         if (!trainRepository.existsById(request.getTrainId())) {
//             throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
//         }
//         Train train = trainRepository.findById(request.getTrainId()).get();
//         existingTrainTrip.setTrain(train);

//         // Cập nhật Route
//         Station originStation = stationRepository.findByStationName(request.getOriginStationName());
//         if (originStation == null) {
//             originStation = new Station();
//             originStation.setStationName(request.getOriginStationName());
//             originStation.setPosition(0.0);
//             originStation = stationRepository.save(originStation);
//         }

//         List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
//             Station station = stationRepository.findByStationName(name);
//             if (station == null) {
//                 station = new Station();
//                 station.setStationName(name);
//                 station.setPosition(0.0);
//                 station = stationRepository.save(station);
//             }
//             return station;
//         }).collect(Collectors.toList());

//         List<Route> routes = routeRepository.findByOriginStation(originStation);
//         Route route = null;
//         for (Route r : routes) {
//             if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
//                 route = r;
//                 break;
//             }
//         }
//         if (route == null) {
//             route = existingTrainTrip.getRoute();
//             route.setOriginStation(originStation);
//             route.setJourney(journeyStations);
//             route = routeRepository.save(route);
//         }
//         existingTrainTrip.setRoute(route);

//         // Cập nhật Schedule
//         ClockTime departure = existingTrainTrip.getSchedule().getDeparture();
//         departure.setDate(request.getDepartureTime());
//         departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

//         ClockTime arrival = existingTrainTrip.getSchedule().getArrival();
//         arrival.setDate(request.getArrivalTime());
//         arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
//                 request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
//         arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

//         clockTimeRepository.save(departure);
//         clockTimeRepository.save(arrival);

//         Schedule schedule = existingTrainTrip.getSchedule();
//         schedule.setDeparture(departure);
//         schedule.setArrival(arrival);
//         schedule = scheduleRepository.save(schedule);
//         existingTrainTrip.setSchedule(schedule);

//         // Cập nhật Seats (xóa seats cũ và tạo mới dựa trên Carriages của Train)
//         List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
//         if (carriages.isEmpty()) {
//             throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
//         }

//         List<Seat> existingSeats = this.seatRepository.findByTrainTrip(existingTrainTrip);
//         seatRepository.deleteAll(existingSeats);

//         for (Carriage carriage : carriages) {
//             int seatCount = switch (carriage.getCarriageType()) {
//                 case sixBeds -> 42;
//                 case fourBeds -> 28;
//                 case seat -> 56;
//             };

//             List<Seat> seats = new ArrayList<>();
//             for (int i = 1; i <= seatCount; i++) {
//                 Seat seat = new Seat();
//                 seat.setSeatStatus(SeatStatusEnum.available);
//                 seat.setCarriage(carriage);
//                 seat.setTrainTrip(existingTrainTrip); // Liên kết Seat với TrainTrip

//                 SeatTypeEnum seatType;
//                 double price;
//                 switch (carriage.getCarriageType()) {
//                     case sixBeds:
//                         if (isSixBedsLevel3(i)) {
//                             seatType = SeatTypeEnum.LEVEL_3;
//                             price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
//                         } else if (isSixBedsLevel2(i)) {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         }
//                         break;
//                     case fourBeds:
//                         if (isFourBedsLevel1(i)) {
//                             seatType = SeatTypeEnum.LEVEL_1;
//                             price = carriage.getPrice();
//                         } else {
//                             seatType = SeatTypeEnum.LEVEL_2;
//                             price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
//                         }
//                         break;
//                     case seat:
//                     default:
//                         seatType = SeatTypeEnum.LEVEL_1;
//                         price = carriage.getPrice();
//                         break;
//                 }

//                 seat.setSeatType(seatType);
//                 seat.setPrice(price);
//                 seats.add(seat);
//             }
//             seatRepository.saveAll(seats);
//         }

//         return trainTripRepository.save(existingTrainTrip);
//     }

//     public void handleDeleteTrainTrip(Long id) throws IdInvalidException {
//         if (!trainTripRepository.existsById(id)) {
//             throw new IdInvalidException("TrainTrip with ID " + id + " does not exist");
//         }
//         trainTripRepository.deleteById(id);
//     }

//     // Helper methods từ CarriageService
//     private boolean isSixBedsLevel3(int seatNumber) {
//         return (seatNumber % 6 == 1 || seatNumber % 6 == 2);
//     }

//     private boolean isSixBedsLevel2(int seatNumber) {
//         return (seatNumber % 6 == 3 || seatNumber % 6 == 4);
//     }

//     private boolean isFourBedsLevel1(int seatNumber) {
//         return (seatNumber % 4 == 3 || seatNumber % 4 == 0);
//     }
// }
/////////////

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.domain.response.TrainTripResponseDTO;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.SeatTypeEnum;
import com.fourt.railskylines.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainTripService {

    private final TrainTripRepository trainTripRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClockTimeRepository clockTimeRepository;
    private final CarriageRepository carriageRepository;
    private final SeatRepository seatRepository;

    public TrainTripService(
            TrainTripRepository trainTripRepository,
            TrainRepository trainRepository,
            StationRepository stationRepository,
            RouteRepository routeRepository,
            ScheduleRepository scheduleRepository,
            ClockTimeRepository clockTimeRepository,
            CarriageRepository carriageRepository,
            SeatRepository seatRepository) {
        this.trainTripRepository = trainTripRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.routeRepository = routeRepository;
        this.scheduleRepository = scheduleRepository;
        this.clockTimeRepository = clockTimeRepository;
        this.carriageRepository = carriageRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public TrainTrip handleCreateTrainTrip(TrainTripRequestDTO request) throws IdInvalidException {
        // 1. Validate Train
        if (!trainRepository.existsById(request.getTrainId())) {
            throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
        }
        Train train = trainRepository.findById(request.getTrainId()).get();

        // 2. Tạo hoặc tìm Station cho originStation
        Station originStation = stationRepository.findByStationName(request.getOriginStationName());
        if (originStation == null) {
            originStation = new Station();
            originStation.setStationName(request.getOriginStationName());
            originStation.setPosition(0.0);
            originStation = stationRepository.save(originStation);
        }

        // 3. Tạo hoặc tìm Station cho journey
        List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
            Station station = stationRepository.findByStationName(name);
            if (station == null) {
                station = new Station();
                station.setStationName(name);
                station.setPosition(0.0);
                station = stationRepository.save(station);
            }
            return station;
        }).collect(Collectors.toList());

        // 4. Tìm hoặc tạo Route
        List<Route> routes = routeRepository.findByOriginStation(originStation);
        Route route = null;
        for (Route r : routes) {
            if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
                route = r;
                break;
            }
        }
        if (route == null) {
            route = new Route();
            route.setOriginStation(originStation);
            route.setJourney(journeyStations);
            route = routeRepository.save(route);
        }

        // 5. Tạo ClockTime
        ClockTime departure = new ClockTime();
        departure.setDate(request.getDepartureTime());
        departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
                request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
        departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

        ClockTime arrival = new ClockTime();
        arrival.setDate(request.getArrivalTime());
        arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
                request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
        arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

        clockTimeRepository.save(departure);
        clockTimeRepository.save(arrival);

        // 6. Tạo Schedule
        Schedule schedule = new Schedule();
        schedule.setDeparture(departure);
        schedule.setArrival(arrival);
        schedule = scheduleRepository.save(schedule);

        // 7. Tạo TrainTrip
        TrainTrip trainTrip = new TrainTrip();
        trainTrip.setTrain(train);
        trainTrip.setRoute(route);
        trainTrip.setSchedule(schedule);
        trainTrip = trainTripRepository.save(trainTrip);

        // 8. Tạo Seats cho TrainTrip dựa trên Carriages của Train
        List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
        if (carriages.isEmpty()) {
            throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
        }

        for (Carriage carriage : carriages) {
            int seatCount = switch (carriage.getCarriageType()) {
                case sixBeds -> 42;
                case fourBeds -> 28;
                case seat -> 56;
            };

            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= seatCount; i++) {
                Seat seat = new Seat();
                seat.setSeatStatus(SeatStatusEnum.available);
                seat.setCarriage(carriage);
                seat.setTrainTrip(trainTrip);

                SeatTypeEnum seatType;
                double price;
                switch (carriage.getCarriageType()) {
                    case sixBeds:
                        if (isSixBedsLevel3(i)) {
                            seatType = SeatTypeEnum.LEVEL_3;
                            price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
                        } else if (isSixBedsLevel2(i)) {
                            seatType = SeatTypeEnum.LEVEL_2;
                            price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
                        } else {
                            seatType = SeatTypeEnum.LEVEL_1;
                            price = carriage.getPrice();
                        }
                        break;
                    case fourBeds:
                        if (isFourBedsLevel1(i)) {
                            seatType = SeatTypeEnum.LEVEL_1;
                            price = carriage.getPrice();
                        } else {
                            seatType = SeatTypeEnum.LEVEL_2;
                            price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
                        }
                        break;
                    case seat:
                    default:
                        seatType = SeatTypeEnum.LEVEL_1;
                        price = carriage.getPrice();
                        break;
                }

                seat.setSeatType(seatType);
                seat.setPrice(price);
                seats.add(seat);
            }
            seatRepository.saveAll(seats);
        }

        return trainTrip;
    }

    public TrainTripResponseDTO fetchTrainTripById(Long id) {
        Optional<TrainTrip> trainTripOptional = trainTripRepository.findById(id);
        if (trainTripOptional.isEmpty()) {
            return null;
        }

        TrainTrip trainTrip = trainTripOptional.get();
        TrainTripResponseDTO response = new TrainTripResponseDTO();
        response.setTrainTripId(trainTrip.getTrainTripId());

        // Map Train and Carriages
        TrainTripResponseDTO.TrainDTO trainDTO = new TrainTripResponseDTO.TrainDTO();
        trainDTO.setTrainId(trainTrip.getTrain().getTrainId());
        trainDTO.setTrainName(trainTrip.getTrain().getTrainName());
        trainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

        // Create SimpleTrainDTO for CarriageDTO to avoid recursion
        TrainTripResponseDTO.SimpleTrainDTO simpleTrainDTO = new TrainTripResponseDTO.SimpleTrainDTO();
        simpleTrainDTO.setTrainId(trainTrip.getTrain().getTrainId());
        simpleTrainDTO.setTrainName(trainTrip.getTrain().getTrainName());
        simpleTrainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

        // Fetch Carriages
        List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
        List<TrainTripResponseDTO.CarriageDTO> carriageDTOs = carriages.stream().map(carriage -> {
            TrainTripResponseDTO.CarriageDTO carriageDTO = new TrainTripResponseDTO.CarriageDTO();
            carriageDTO.setCarriageId(carriage.getCarriageId());
            carriageDTO.setCarriageType(carriage.getCarriageType().toString());
            carriageDTO.setPrice(carriage.getPrice());
            carriageDTO.setDiscount(carriage.getDiscount());
            carriageDTO.setTrain(simpleTrainDTO);

            // Fetch Seats for this Carriage
            List<Seat> seats = seatRepository.findByCarriageCarriageIdAndTrainTripTrainTripId(carriage.getCarriageId(),
                    trainTrip.getTrainTripId());
            List<TrainTripResponseDTO.SeatDTO> seatDTOs = seats.stream().map(seat -> {
                TrainTripResponseDTO.SeatDTO seatDTO = new TrainTripResponseDTO.SeatDTO();
                seatDTO.setSeatId(seat.getSeatId());
                seatDTO.setSeatType(seat.getSeatType());
                seatDTO.setSeatStatus(seat.getSeatStatus());
                // Calculate price based on seatType and carriage
                double price;
                switch (seat.getSeatType()) {
                    case LEVEL_3:
                        price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
                        break;
                    case LEVEL_2:
                        price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
                        break;
                    case LEVEL_1:
                    default:
                        price = carriage.getPrice();
                        break;
                }
                seatDTO.setPrice(price);
                return seatDTO;
            }).collect(Collectors.toList());

            carriageDTO.setSeats(seatDTOs);
            return carriageDTO;
        }).collect(Collectors.toList());

        trainDTO.setCarriages(carriageDTOs);
        response.setTrain(trainDTO);
        response.setRoute(trainTrip.getRoute());
        response.setSchedule(trainTrip.getSchedule());

        return response;

    }

    public ResultPaginationDTO fetchAllTrainTrips(Specification<TrainTrip> spec, Pageable pageable) {
        Page<TrainTrip> pageTrainTrip = trainTripRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageTrainTrip.getTotalPages());
        mt.setTotal(pageTrainTrip.getTotalElements());
        rs.setMeta(mt);

        // Map TrainTrip entities to TrainTripResponseDTO
        List<TrainTripResponseDTO> trainTripDTOs = pageTrainTrip.getContent().stream().map(trainTrip -> {
            TrainTripResponseDTO response = new TrainTripResponseDTO();
            response.setTrainTripId(trainTrip.getTrainTripId());

            // Map Train and Carriages
            TrainTripResponseDTO.TrainDTO trainDTO = new TrainTripResponseDTO.TrainDTO();
            trainDTO.setTrainId(trainTrip.getTrain().getTrainId());
            trainDTO.setTrainName(trainTrip.getTrain().getTrainName());
            trainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

            // Create SimpleTrainDTO for CarriageDTO to avoid recursion
            TrainTripResponseDTO.SimpleTrainDTO simpleTrainDTO = new TrainTripResponseDTO.SimpleTrainDTO();
            simpleTrainDTO.setTrainId(trainTrip.getTrain().getTrainId());
            simpleTrainDTO.setTrainName(trainTrip.getTrain().getTrainName());
            simpleTrainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

            // Fetch Carriages
            List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
            List<TrainTripResponseDTO.CarriageDTO> carriageDTOs = carriages.stream().map(carriage -> {
                TrainTripResponseDTO.CarriageDTO carriageDTO = new TrainTripResponseDTO.CarriageDTO();
                carriageDTO.setCarriageId(carriage.getCarriageId());
                carriageDTO.setCarriageType(carriage.getCarriageType().toString());
                carriageDTO.setPrice(carriage.getPrice());
                carriageDTO.setDiscount(carriage.getDiscount());
                carriageDTO.setTrain(simpleTrainDTO);

                // Fetch Seats for this Carriage
                List<Seat> seats = seatRepository.findByCarriageCarriageIdAndTrainTripTrainTripId(
                        carriage.getCarriageId(), trainTrip.getTrainTripId());
                List<TrainTripResponseDTO.SeatDTO> seatDTOs = seats.stream().map(seat -> {
                    TrainTripResponseDTO.SeatDTO seatDTO = new TrainTripResponseDTO.SeatDTO();
                    seatDTO.setSeatId(seat.getSeatId());
                    seatDTO.setSeatType(seat.getSeatType());
                    seatDTO.setSeatStatus(seat.getSeatStatus());
                    // Calculate price based on seatType and carriage
                    double price;
                    switch (seat.getSeatType()) {
                        case LEVEL_3:
                            price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
                            break;
                        case LEVEL_2:
                            price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
                            break;
                        case LEVEL_1:
                        default:
                            price = carriage.getPrice();
                            break;
                    }
                    seatDTO.setPrice(price);
                    return seatDTO;
                }).collect(Collectors.toList());

                carriageDTO.setSeats(seatDTOs);
                return carriageDTO;
            }).collect(Collectors.toList());

            trainDTO.setCarriages(carriageDTOs);
            response.setTrain(trainDTO);
            response.setRoute(trainTrip.getRoute());
            response.setSchedule(trainTrip.getSchedule());

            return response;

        }).collect(Collectors.toList());

        rs.setResult(trainTripDTOs);
        return rs;
    }

    @Transactional
    public TrainTrip handleUpdateTrainTrip(Long id, TrainTripRequestDTO request) throws IdInvalidException {
        TrainTrip existingTrainTrip = trainTripRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("TrainTrip with ID " + id + " does not exist"));

        // Cập nhật Train
        if (!trainRepository.existsById(request.getTrainId())) {
            throw new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist");
        }
        Train train = trainRepository.findById(request.getTrainId()).get();
        existingTrainTrip.setTrain(train);

        // Cập nhật Route
        Station originStation = stationRepository.findByStationName(request.getOriginStationName());
        if (originStation == null) {
            originStation = new Station();
            originStation.setStationName(request.getOriginStationName());
            originStation.setPosition(0.0);
            originStation = stationRepository.save(originStation);
        }

        List<Station> journeyStations = request.getJourneyStationNames().stream().map(name -> {
            Station station = stationRepository.findByStationName(name);
            if (station == null) {
                station = new Station();
                station.setStationName(name);
                station.setPosition(0.0);
                station = stationRepository.save(station);
            }
            return station;
        }).collect(Collectors.toList());

        List<Route> routes = routeRepository.findByOriginStation(originStation);
        Route route = null;
        for (Route r : routes) {
            if (r.getJourney().size() == journeyStations.size() && r.getJourney().containsAll(journeyStations)) {
                route = r;
                break;
            }
        }
        if (route == null) {
            route = existingTrainTrip.getRoute();
            route.setOriginStation(originStation);
            route.setJourney(journeyStations);
            route = routeRepository.save(route);
        }
        existingTrainTrip.setRoute(route);

        // Cập nhật Schedule
        ClockTime departure = existingTrainTrip.getSchedule().getDeparture();
        departure.setDate(request.getDepartureTime());
        departure.setHour(request.getDepartureTime().atZone(ZoneId.systemDefault()).getHour() +
                request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute() / 60.0);
        departure.setMinute(request.getDepartureTime().atZone(ZoneId.systemDefault()).getMinute());

        ClockTime arrival = existingTrainTrip.getSchedule().getArrival();
        arrival.setDate(request.getArrivalTime());
        arrival.setHour(request.getArrivalTime().atZone(ZoneId.systemDefault()).getHour() +
                request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute() / 100);
        arrival.setMinute(request.getArrivalTime().atZone(ZoneId.systemDefault()).getMinute());

        clockTimeRepository.save(departure);
        clockTimeRepository.save(arrival);

        Schedule schedule = existingTrainTrip.getSchedule();
        schedule.setDeparture(departure);
        schedule.setArrival(arrival);
        schedule = scheduleRepository.save(schedule);
        existingTrainTrip.setSchedule(schedule);

        // Cập nhật Seats (xóa seats cũ và tạo mới dựa trên Carriages của Train)
        List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
        if (carriages.isEmpty()) {
            throw new IdInvalidException("No carriages available for train with ID " + request.getTrainId());
        }

        List<Seat> existingSeats = this.seatRepository.findByTrainTrip(existingTrainTrip);
        seatRepository.deleteAll(existingSeats);

        for (Carriage carriage : carriages) {
            int seatCount = switch (carriage.getCarriageType()) {
                case sixBeds -> 42;
                case fourBeds -> 28;
                case seat -> 56;
            };

            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= seatCount; i++) {
                Seat seat = new Seat();
                seat.setSeatStatus(SeatStatusEnum.available);
                seat.setCarriage(carriage);
                seat.setTrainTrip(existingTrainTrip);

                SeatTypeEnum seatType;
                double price;
                switch (carriage.getCarriageType()) {
                    case sixBeds:
                        if (isSixBedsLevel3(i)) {
                            seatType = SeatTypeEnum.LEVEL_3;
                            price = carriage.getPrice() * (100 - 2 * carriage.getDiscount()) / 100;
                        } else if (isSixBedsLevel2(i)) {
                            seatType = SeatTypeEnum.LEVEL_2;
                            price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
                        } else {
                            seatType = SeatTypeEnum.LEVEL_1;
                            price = carriage.getPrice();
                        }
                        break;
                    case fourBeds:
                        if (isFourBedsLevel1(i)) {
                            seatType = SeatTypeEnum.LEVEL_1;
                            price = carriage.getPrice();
                        } else {
                            seatType = SeatTypeEnum.LEVEL_2;
                            price = carriage.getPrice() * (100 - carriage.getDiscount()) / 100;
                        }
                        break;
                    case seat:
                    default:
                        seatType = SeatTypeEnum.LEVEL_1;
                        price = carriage.getPrice();
                        break;
                }

                seat.setSeatType(seatType);
                seat.setPrice(price);
                seats.add(seat);
            }
            seatRepository.saveAll(seats);
        }

        return trainTripRepository.save(existingTrainTrip);
    }

    public void handleDeleteTrainTrip(Long id) throws IdInvalidException {
        if (!trainTripRepository.existsById(id)) {
            throw new IdInvalidException("TrainTrip with ID " + id + " does not exist");
        }
        trainTripRepository.deleteById(id);
    }

    // Helper methods từ CarriageService
    private boolean isSixBedsLevel3(int seatNumber) {
        return (seatNumber % 6 == 1 || seatNumber % 6 == 2);
    }

    private boolean isSixBedsLevel2(int seatNumber) {
        return (seatNumber % 6 == 3 || seatNumber % 6 == 4);
    }

    private boolean isFourBedsLevel1(int seatNumber) {
        return (seatNumber % 4 == 3 || seatNumber % 4 == 0);
    }
}