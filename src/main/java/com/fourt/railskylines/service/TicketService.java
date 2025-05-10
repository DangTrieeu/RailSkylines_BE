package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.domain.response.TicketResponseDTO;
import com.fourt.railskylines.repository.BookingRepository;
import com.fourt.railskylines.repository.TicketRepository;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    public TicketService(TicketRepository ticketRepository, BookingRepository bookingRepository) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
    }

    public Ticket findByTicketCodeAndCitizenId(String ticketCode, String citizenId) {
        return ticketRepository.findByTicketCodeAndCitizenId(ticketCode, citizenId)
                .orElseThrow(() -> new RuntimeException("Ticket not found or citizen ID does not match"));
    }

    public Booking findBookingByCodeAndCitizenId(String bookingCode, String citizenId) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        boolean isValid = booking.getTickets().stream()
                .anyMatch(ticket -> ticket.getCitizenId().equals(citizenId));
        if (!isValid) {
            throw new RuntimeException("Citizen ID does not match any ticket in this booking");
        }
        return booking;
    }

    public TicketResponseDTO getTicketByCode(String ticketCode) {
        Optional<Ticket> ticketOpt = this.ticketRepository.findByTicketCode(ticketCode);
        if (ticketOpt.isEmpty()) {
            throw new RuntimeException("Ticket not found for ticketCode: " + ticketCode);
        }
        Ticket ticket = ticketOpt.get();

        // Map to TicketResponseDTO
        TicketResponseDTO ticketDTO = new TicketResponseDTO();
        ticketDTO.setTicketId(ticket.getTicketId());
        ticketDTO.setCustomerObject(ticket.getCustomerObject());
        ticketDTO.setTicketCode(ticket.getTicketCode());
        ticketDTO.setName(ticket.getName());
        ticketDTO.setCitizenId(ticket.getCitizenId());
        ticketDTO.setPrice(ticket.getPrice());
        ticketDTO.setStartDay(ticket.getStartDay());
        ticketDTO.setTicketStatus(ticket.getTicketStatus());

        // Map Seat
        TicketResponseDTO.SeatDTO seatDTO = new TicketResponseDTO.SeatDTO();
        seatDTO.setSeatId(ticket.getSeat().getSeatId());
        seatDTO.setPrice(ticket.getSeat().getPrice());
        seatDTO.setSeatStatus(ticket.getSeat().getSeatStatus().name());
        ticketDTO.setSeat(seatDTO);

        // Map TrainTrip
        TicketResponseDTO.TrainTripDTO trainTripDTO = new TicketResponseDTO.TrainTripDTO();
        trainTripDTO.setTrainTripId(ticket.getTrainTrip().getTrainTripId());
        // trainTripDTO.setDeparture(ticket.getTrainTrip().getDeparture());
        // trainTripDTO.setArrival(ticket.getTrainTrip().getArrival());

        // Map Train
        TicketResponseDTO.TrainTripDTO.TrainDTO trainDTO = new TicketResponseDTO.TrainTripDTO.TrainDTO();
        trainDTO.setTrainId(ticket.getTrainTrip().getTrain().getTrainId());
        trainDTO.setTrainName(ticket.getTrainTrip().getTrain().getTrainName());
        trainTripDTO.setTrain(trainDTO);

        ticketDTO.setTrainTrip(trainTripDTO);
        return ticketDTO;
    }
}