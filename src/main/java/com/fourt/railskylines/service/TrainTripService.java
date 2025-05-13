package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.TrainTripRequestDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.domain.response.TrainTripResponseDTO;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.SeatTypeEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import com.fourt.railskylines.util.error.IdInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrainTripService {
    private static final Logger logger = LoggerFactory.getLogger(TrainTripService.class);

    private final TrainTripRepository trainTripRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final ClockTimeRepository clockTimeRepository;
    private final CarriageRepository carriageRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    public TrainTripService(
            TrainTripRepository trainTripRepository,
            TrainRepository trainRepository,
            StationRepository stationRepository,
            RouteRepository routeRepository,
            ScheduleRepository scheduleRepository,
            ClockTimeRepository clockTimeRepository,
            CarriageRepository carriageRepository,
            SeatRepository seatRepository,
            TicketRepository ticketRepository) {
        this.trainTripRepository = trainTripRepository;
        this.trainRepository = trainRepository;
        this.stationRepository = stationRepository;
        this.routeRepository = routeRepository;
        this.scheduleRepository = scheduleRepository;
        this.clockTimeRepository = clockTimeRepository;
        this.carriageRepository = carriageRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TrainTrip handleCreateTrainTrip(TrainTripRequestDTO request) throws IdInvalidException {
        // 1. Validate Train
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist"));

        // 2. Tạo hoặc tìm Station cho originStation và journey
        Station originStation = stationRepository.findByStationName(request.getOriginStationName());
        if (originStation == null) {
            originStation = new Station();
            originStation.setStationName(request.getOriginStationName());
            originStation.setPosition(0.0);
            originStation = stationRepository.save(originStation);
        }

        List<Station> journeyStations = new ArrayList<>();
        for (int i = 0; i < request.getJourneyStationNames().size(); i++) {
            String name = request.getJourneyStationNames().get(i);
            Station station = stationRepository.findByStationName(name);
            if (station == null) {
                station = new Station();
                station.setStationName(name);
                station.setPosition(i + 1.0);
                station = stationRepository.save(station);
            } else if (station.getPosition() != i + 1.0) {
                station.setPosition(i + 1.0);
                station = stationRepository.save(station);
            }
            journeyStations.add(station);
        }

        // 3. Tìm hoặc tạo Route
        Optional<Route> routeOpt = routeRepository.findByOriginStationNameAndJourneyStationNames(
                request.getOriginStationName(), request.getJourneyStationNames(),
                (long) request.getJourneyStationNames().size());
        Route route;
        if (routeOpt.isEmpty()) {
            route = new Route();
            route.setOriginStation(originStation);
            route.setJourney(journeyStations);
            route = routeRepository.save(route);
            logger.info("Created new route with ID: {}", route.getRouteId());
        } else {
            route = routeOpt.get();
            logger.info("Found existing route with ID: {}", route.getRouteId());
        }

        // 4. Tạo ClockTime
        ClockTime departure = new ClockTime();
        departure.setDate(Instant.parse(request.getDepartureTime()));

        ClockTime arrival = new ClockTime();
        arrival.setDate(Instant.parse(request.getArrivalTime()));

        clockTimeRepository.saveAll(List.of(departure, arrival));

        // 5. Tạo Schedule
        Schedule schedule = new Schedule();
        schedule.setDeparture(departure);
        schedule.setArrival(arrival);
        schedule = scheduleRepository.save(schedule);

        // 6. Tạo TrainTrip
        TrainTrip trainTrip = new TrainTrip();
        trainTrip.setTrain(train);
        trainTrip.setRoute(route);
        trainTrip.setSchedule(schedule);
        trainTrip = trainTripRepository.save(trainTrip);

        // 7. Tạo Seats cho TrainTrip
        createSeatsForTrainTrip(trainTrip, train);

        logger.info("Created TrainTrip with ID: {}", trainTrip.getTrainTripId());
        return trainTrip;
    }

    @Transactional
    public TrainTrip handleUpdateTrainTrip(Long id, TrainTripRequestDTO request) throws IdInvalidException {
        TrainTrip existingTrainTrip = trainTripRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("TrainTrip with ID " + id + " does not exist"));

        // 1. Cập nhật Train
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new IdInvalidException("Train with ID " + request.getTrainId() + " does not exist"));
        existingTrainTrip.setTrain(train);

        // 2. Cập nhật Route
        Station originStation = stationRepository.findByStationName(request.getOriginStationName());
        if (originStation == null) {
            originStation = new Station();
            originStation.setStationName(request.getOriginStationName());
            originStation.setPosition(0.0);
            originStation = stationRepository.save(originStation);
        }

        List<Station> journeyStations = new ArrayList<>();
        for (int i = 0; i < request.getJourneyStationNames().size(); i++) {
            String name = request.getJourneyStationNames().get(i);
            Station station = stationRepository.findByStationName(name);
            if (station == null) {
                station = new Station();
                station.setStationName(name);
                station.setPosition(i + 1.0);
                station = stationRepository.save(station);
            } else if (station.getPosition() != i + 1.0) {
                station.setPosition(i + 1.0);
                station = stationRepository.save(station);
            }
            journeyStations.add(station);
        }

        Optional<Route> routeOpt = routeRepository.findByOriginStationNameAndJourneyStationNames(
                request.getOriginStationName(), request.getJourneyStationNames(),
                (long) request.getJourneyStationNames().size());
        Route route;
        if (routeOpt.isEmpty()) {
            route = existingTrainTrip.getRoute();
            route.setOriginStation(originStation);
            route.setJourney(journeyStations);
            route = routeRepository.save(route);
            logger.info("Updated route with ID: {}", route.getRouteId());
        } else {
            route = routeOpt.get();
            logger.info("Found existing route with ID: {}", route.getRouteId());
        }
        existingTrainTrip.setRoute(route);

        // 3. Cập nhật Schedule
        ClockTime departure = existingTrainTrip.getSchedule().getDeparture();
        departure.setDate(Instant.parse(request.getDepartureTime()));

        ClockTime arrival = existingTrainTrip.getSchedule().getArrival();
        arrival.setDate(Instant.parse(request.getArrivalTime()));

        clockTimeRepository.saveAll(List.of(departure, arrival));

        Schedule schedule = existingTrainTrip.getSchedule();
        schedule.setDeparture(departure);
        schedule.setArrival(arrival);
        scheduleRepository.save(schedule);

        // 4. Cập nhật Seats: Xóa seats cũ và tạo mới
        List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
        for (Carriage carriage : carriages) {
            List<Seat> seats = seatRepository.findByCarriageCarriageId(carriage.getCarriageId());
            if (ticketRepository.existsBySeatInAndTicketStatusIn(
                    seats, List.of(TicketStatusEnum.issued, TicketStatusEnum.used))) {
                throw new IdInvalidException("Cannot update TrainTrip with active tickets");
            }
            seatRepository.deleteByCarriage(carriage);
        }
        createSeatsForTrainTrip(existingTrainTrip, train);

        TrainTrip updatedTrainTrip = trainTripRepository.save(existingTrainTrip);
        logger.info("Updated TrainTrip with ID: {}", updatedTrainTrip.getTrainTripId());
        return updatedTrainTrip;
    }

    public TrainTripResponseDTO fetchTrainTripById(Long id) {
        TrainTrip trainTrip = trainTripRepository.findById(id)
                .orElse(null);
        if (trainTrip == null) {
            return null;
        }

        return mapToTrainTripResponseDTO(trainTrip);
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

        List<TrainTripResponseDTO> trainTripDTOs = pageTrainTrip.getContent().stream()
                .map(this::mapToTrainTripResponseDTO)
                .collect(Collectors.toList());

        rs.setResult(trainTripDTOs);
        return rs;
    }

    @Transactional
    public void handleDeleteTrainTrip(Long id) throws IdInvalidException {
        TrainTrip trainTrip = trainTripRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("TrainTrip with ID " + id + " does not exist"));

        // Kiểm tra xem có vé đang hoạt động không
        List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
        for (Carriage carriage : carriages) {
            List<Seat> seats = seatRepository.findByCarriageCarriageId(carriage.getCarriageId());
            if (ticketRepository.existsBySeatInAndTicketStatusIn(
                    seats, List.of(TicketStatusEnum.issued, TicketStatusEnum.used))) {
                throw new IdInvalidException("Cannot delete TrainTrip with active tickets");
            }
        }

        trainTripRepository.deleteById(id);
        logger.info("Deleted TrainTrip with ID: {}", id);
    }

    private void createSeatsForTrainTrip(TrainTrip trainTrip, Train train) throws IdInvalidException {
        List<Carriage> carriages = carriageRepository.findByTrainTrainId(train.getTrainId());
        if (carriages.isEmpty()) {
            throw new IdInvalidException("No carriages available for train with ID " + train.getTrainId());
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
    }

    private TrainTripResponseDTO mapToTrainTripResponseDTO(TrainTrip trainTrip) {
        TrainTripResponseDTO response = new TrainTripResponseDTO();
        response.setTrainTripId(trainTrip.getTrainTripId());

        TrainTripResponseDTO.TrainDTO trainDTO = new TrainTripResponseDTO.TrainDTO();
        trainDTO.setTrainId(trainTrip.getTrain().getTrainId());
        trainDTO.setTrainName(trainTrip.getTrain().getTrainName());
        trainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

        TrainTripResponseDTO.SimpleTrainDTO simpleTrainDTO = new TrainTripResponseDTO.SimpleTrainDTO();
        simpleTrainDTO.setTrainId(trainTrip.getTrain().getTrainId());
        simpleTrainDTO.setTrainName(trainTrip.getTrain().getTrainName());
        simpleTrainDTO.setTrainStatus(trainTrip.getTrain().getTrainStatus());

        List<Carriage> carriages = carriageRepository.findByTrainTrainId(trainTrip.getTrain().getTrainId());
        List<TrainTripResponseDTO.CarriageDTO> carriageDTOs = carriages.stream().map(carriage -> {
            TrainTripResponseDTO.CarriageDTO carriageDTO = new TrainTripResponseDTO.CarriageDTO();
            carriageDTO.setCarriageId(carriage.getCarriageId());
            carriageDTO.setCarriageType(carriage.getCarriageType().toString());
            carriageDTO.setPrice(carriage.getPrice());
            carriageDTO.setDiscount(carriage.getDiscount());
            carriageDTO.setTrain(simpleTrainDTO);

            List<Seat> seats = seatRepository.findByCarriageCarriageId(carriage.getCarriageId());
            List<TrainTripResponseDTO.SeatDTO> seatDTOs = seats.stream().map(seat -> {
                TrainTripResponseDTO.SeatDTO seatDTO = new TrainTripResponseDTO.SeatDTO();
                seatDTO.setSeatId(seat.getSeatId());
                seatDTO.setSeatType(seat.getSeatType());
                seatDTO.setSeatStatus(seat.getSeatStatus());
                seatDTO.setPrice(seat.getPrice());
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