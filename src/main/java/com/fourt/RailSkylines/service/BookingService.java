package com.fourt.RailSkylines.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fourt.RailSkylines.domain.Booking;
import com.fourt.RailSkylines.domain.Carriage;
import com.fourt.RailSkylines.domain.Seat;
import com.fourt.RailSkylines.domain.Ticket;
import com.fourt.RailSkylines.domain.Train;
import com.fourt.RailSkylines.domain.TrainTrip;
import com.fourt.RailSkylines.domain.User;
import com.fourt.RailSkylines.repository.BookingRepository;
import com.fourt.RailSkylines.repository.SeatRepository;
import com.fourt.RailSkylines.repository.TicketRepository;
import com.fourt.RailSkylines.repository.TrainTripRepository;
import com.fourt.RailSkylines.util.constant.CustomerObjectEnum;
import com.fourt.RailSkylines.util.constant.SeatStatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final TrainTripRepository trainTripRepository;

    public List<TrainTrip> findTrips(String from, String to, Instant date) {
        return trainTripRepository.findTrainTripsByStationNamesAndDate(from, to, date);
    }

    public List<Seat> getAvailableSeats(Long trainTripId) {
        TrainTrip trip = trainTripRepository.findById(trainTripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return trip.getTrain().getCarriages().stream()
                .flatMap(carriage -> carriage.getSeats().stream())
                .filter(seat -> seat.getSeatStatus() == SeatStatusEnum.available)
                .collect(Collectors.toList());
    }

    public Ticket addSeatToCart(User user, Long seatId, CustomerObjectEnum customerType) {
        Seat seat = seatRepository.findById(seatId)
                .filter(s -> s.getSeatStatus() == SeatStatusEnum.available)
                .orElseThrow(() -> new RuntimeException("Seat not available"));

        seat.setSeatStatus(SeatStatusEnum.pending);
        seatRepository.save(seat);

        // Tìm TrainTrip từ Train qua Carriage
        Carriage carriage = seat.getCarriage();
        Train train = carriage.getTrain();
        TrainTrip trainTrip = trainTripRepository.findByTrain(train)
                .orElseThrow(() -> new RuntimeException("TrainTrip not found for selected seat"));

        Ticket ticket = new Ticket();
        ticket.setOwner(user);
        ticket.setSeat(seat);
        ticket.setCustomerObject(customerType);
        ticket.setStartDay(trainTrip.getSchedule().getArrival()); // gán theo thời gian chuyến đi

        return ticketRepository.save(ticket);
    }

    public Booking createBooking(User user, List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);

        Booking booking = new Booking();
        booking.setContactInfor(user.getEmail());
        booking.setDate(Instant.now());
        booking.setPaymentStatus("pending");

        booking.setTickets(tickets);
        for (Ticket ticket : tickets) {
            ticket.setBooking(booking);
        }

        return bookingRepository.save(booking);
    }

    public Booking completePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // booking.setPaymentStatus("PAID");
        // booking.setPermissionName("CONFIRMED");

        for (Ticket ticket : booking.getTickets()) {
            ticket.setPayAt(Instant.now());
            Seat seat = ticket.getSeat();
            seat.setSeatStatus(SeatStatusEnum.booked);
            seatRepository.save(seat);
        }

        return bookingRepository.save(booking);
    }
}
