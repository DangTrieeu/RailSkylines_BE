package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.response.ResTicketHistoryDTO;
import com.fourt.railskylines.repository.TicketRepository;
import com.fourt.railskylines.repository.UserRepository;
import com.fourt.railskylines.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    /**
     * Find a ticket by ticket code and citizen ID, no user authentication required.
     */
    public Ticket findByTicketCodeAndCitizenId(String ticketCode, String citizenId) {
        return ticketRepository.findByTicketCodeAndCitizenId(ticketCode, citizenId)
                .orElseThrow(() -> new RuntimeException("Ticket not found or citizen ID does not match"));
    }

    /**
     * Retrieve ticket history for the authenticated user, formatted as
     * ResTicketHistoryDTO.
     */
    public List<ResTicketHistoryDTO> getTicketHistoryByUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        List<Ticket> tickets = ticketRepository.findByOwner(user);
        return tickets.stream().map(ticket -> {
            ResTicketHistoryDTO dto = new ResTicketHistoryDTO();
            dto.setTicketCode(ticket.getTicketCode());
            dto.setName(ticket.getName());
            dto.setCitizenId(ticket.getCitizenId());
            dto.setSeatId(ticket.getSeat().getSeatId());
            dto.setPrice(ticket.getPrice());

            Seat seat = ticket.getSeat();
            Carriage carriage = seat.getCarriage();
            Train train = carriage.getTrain();
            List<TrainTrip> trainTrips = train.getTrip();

            // Tìm TrainTrip phù hợp
            TrainTrip trainTrip = trainTrips.stream()
                    .filter(trip -> {
                        // Tạo danh sách ga đầy đủ: originStation + journey
                        List<Station> allStations = new ArrayList<>();
                        allStations.add(trip.getRoute().getOriginStation());
                        allStations.addAll(trip.getRoute().getJourney());
                        // Kiểm tra xem boardingOrder và alightingOrder có hợp lệ
                        return ticket.getBoardingOrder() >= 0 &&
                                ticket.getBoardingOrder() < allStations.size() &&
                                ticket.getAlightingOrder() > ticket.getBoardingOrder() &&
                                ticket.getAlightingOrder() < allStations.size();
                    })
                    .findFirst()
                    .orElse(null);

            if (trainTrip == null) {
                logger.warn("Không tìm thấy TrainTrip cho vé: {}", ticket.getTicketCode());
                return dto; // Trả về DTO với dữ liệu vé nhưng không có thông tin TrainTrip
            }

            Route route = trainTrip.getRoute();
            // Tạo danh sách ga đầy đủ
            List<Station> allStations = new ArrayList<>();
            allStations.add(route.getOriginStation());
            allStations.addAll(route.getJourney());

            // Lấy ga lên và ga xuống dựa trên boardingOrder và alightingOrder
            if (ticket.getBoardingOrder() >= 0 && ticket.getBoardingOrder() < allStations.size() &&
                    ticket.getAlightingOrder() >= 0 && ticket.getAlightingOrder() < allStations.size()) {
                Station boardingStation = allStations.get(ticket.getBoardingOrder());
                Station alightingStation = allStations.get(ticket.getAlightingOrder());
                dto.setBoardingStationName(boardingStation.getStationName());
                dto.setAlightingStationName(alightingStation.getStationName());
            } else {
                logger.warn(
                        "boardingOrder hoặc alightingOrder không hợp lệ cho vé: {}, boardingOrder={}, alightingOrder={}",
                        ticket.getTicketCode(), ticket.getBoardingOrder(), ticket.getAlightingOrder());
            }

            dto.setCarriageName(carriage.getCarriageType().toString());
            dto.setTrainName(train.getTrainName());

            Schedule schedule = trainTrip.getSchedule();
            dto.setStartDay(schedule.getDeparture().getDate());

            return dto;
        }).collect(Collectors.toList());
    }
    /**
     * Update ticket information.
     */

}