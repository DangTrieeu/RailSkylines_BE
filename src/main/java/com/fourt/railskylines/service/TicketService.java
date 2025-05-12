package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.response.ResTicketHistoryDTO;
import com.fourt.railskylines.repository.TicketRepository;
import com.fourt.railskylines.repository.UserRepository;
import com.fourt.railskylines.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

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
     * Retrieve ticket history for the authenticated user, formatted as ResTicketHistoryDTO.
     */
    public List<ResTicketHistoryDTO> getTicketHistoryByUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
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
            dto.setCarriageName(carriage.getCarriageType().toString());
            dto.setTrainName(train.getTrainName());

            Booking booking = ticket.getBooking();
            List<TrainTrip> trainTrips = train.getTrip();
            TrainTrip trainTrip = trainTrips.stream()
                    .filter(trip -> {
                        List<Station> journey = trip.getRoute().getJourney();
                        boolean hasBoarding = journey.stream()
                                .anyMatch(station -> (int) Math.round(station.getPosition()) == ticket.getBoardingOrder());
                        boolean hasAlighting = journey.stream()
                                .anyMatch(station -> (int) Math.round(station.getPosition()) == ticket.getAlightingOrder());
                        return hasBoarding && hasAlighting;
                    })
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("TrainTrip not found for ticket: " + ticket.getTicketCode()));

            Route route = trainTrip.getRoute();
            List<Station> journey = route.getJourney();

            Station boardingStation = journey.stream()
                    .filter(station -> (int) Math.round(station.getPosition()) == ticket.getBoardingOrder())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Boarding station not found for order: " + ticket.getBoardingOrder()));
            Station alightingStation = journey.stream()
                    .filter(station -> (int) Math.round(station.getPosition()) == ticket.getAlightingOrder())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Alighting station not found for order: " + ticket.getAlightingOrder()));

            dto.setBoardingStationName(boardingStation.getStationName());
            dto.setAlightingStationName(alightingStation.getStationName());

            Schedule schedule = trainTrip.getSchedule();
            dto.setStartDay(schedule.getDeparture().getDate());

            return dto;
        }).collect(Collectors.toList());
    }
}