package com.fourt.railskylines.template;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.repository.BookingRepository;
import com.fourt.railskylines.repository.PromotionRepository;
import com.fourt.railskylines.repository.SeatRepository;
import com.fourt.railskylines.repository.StationRepository;
import com.fourt.railskylines.repository.TicketRepository;
import com.fourt.railskylines.repository.TrainTripRepository;
import com.fourt.railskylines.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BookingCreator {
    protected static final Logger logger = LoggerFactory.getLogger(BookingCreator.class);
    protected final SeatRepository seatRepository;
    protected final BookingRepository bookingRepository;
    protected final TicketRepository ticketRepository;
    protected final PromotionRepository promotionRepository;
    protected final UserRepository userRepository;
    protected final ObjectMapper objectMapper;
    protected final StationRepository stationRepository;
    protected final TrainTripRepository trainTripRepository;

    public BookingCreator(
            SeatRepository seatRepository,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            PromotionRepository promotionRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            StationRepository stationRepository,
            TrainTripRepository trainTripRepository) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.stationRepository = stationRepository;
        this.trainTripRepository = trainTripRepository;
    }

    // Template method: Định nghĩa quy trình tạo Booking
    public final Booking createBooking(BookingRequestDTO request, HttpServletRequest httpServletRequest) {
        validateRequest(request, httpServletRequest);
        Booking booking = initializeBooking(request);
        List<Seat> seats = processSeats(request, booking);
        List<Ticket> tickets = processTickets(request, booking, seats);
        applyPromotion(request, booking, tickets);
        saveBooking(booking, tickets);
        return booking;
    }

    protected abstract void validateRequest(BookingRequestDTO request, HttpServletRequest httpServletRequest);

    protected abstract Booking initializeBooking(BookingRequestDTO request);

    protected abstract List<Seat> processSeats(BookingRequestDTO request, Booking booking);

    protected abstract List<Ticket> processTickets(BookingRequestDTO request, Booking booking, List<Seat> seats);

    protected abstract void applyPromotion(BookingRequestDTO request, Booking booking, List<Ticket> tickets);

    // Hook method: Có thể ghi đè nếu cần
    protected void saveBooking(Booking booking, List<Ticket> tickets) {
        booking = bookingRepository.save(booking);
        ticketRepository.saveAll(tickets);
    }
}