package com.fourt.railskylines.template;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.domain.request.TicketRequestDTO;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.util.SecurityUtil;
import com.fourt.railskylines.util.constant.CustomerObjectEnum;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import com.fourt.railskylines.util.constant.PromotionStatusEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StandardBookingCreator extends BookingCreator {
    private static final Logger logger = LoggerFactory.getLogger(StandardBookingCreator.class);

    public StandardBookingCreator(
            SeatRepository seatRepository,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            PromotionRepository promotionRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            StationRepository stationRepository,
            TrainTripRepository trainTripRepository) {
        super(seatRepository, bookingRepository, ticketRepository, promotionRepository,
                userRepository, objectMapper, stationRepository, trainTripRepository);
    }

    @Override
    protected void validateRequest(BookingRequestDTO request, HttpServletRequest httpServletRequest) {
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (email == null && (request.getContactEmail() == null || request.getContactEmail().isBlank())) {
            throw new RuntimeException("Contact email is required for non-registered users");
        }
        if (request.getTrainTripId() == null) {
            throw new RuntimeException("trainTripId is required");
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new RuntimeException("Seat IDs must not be empty");
        }
        if (request.getTickets().size() != request.getSeatIds().size()) {
            throw new RuntimeException("Số lượng ghế không khớp với số lượng vé");
        }
        if (request.getTicketsParam() != null && !request.getTicketsParam().isEmpty()) {
            try {
                List<Map<String, Object>> ticketParams = objectMapper.readValue(request.getTicketsParam(), List.class);
                if (ticketParams.size() != request.getTickets().size()) {
                    throw new RuntimeException("Số lượng vé trong ticketsParam không khớp với số lượng vé trong body");
                }
                for (int i = 0; i < ticketParams.size(); i++) {
                    Map<String, Object> ticketParam = ticketParams.get(i);
                    Object boardingStationIdObj = ticketParam.get("boardingStationId");
                    Object alightingStationIdObj = ticketParam.get("alightingStationId");
                    Object seatNumberObj = ticketParam.get("seatNumber");
                    Object priceObj = ticketParam.get("price");
                    if (!(boardingStationIdObj instanceof Number) || !(alightingStationIdObj instanceof Number) ||
                            !(seatNumberObj instanceof Number) || !(priceObj instanceof Number)) {
                        throw new RuntimeException("Invalid ticket parameters at index " + i);
                    }
                }
            } catch (Exception e) {
                logger.error("Error parsing ticket params: {}", e.getMessage(), e);
                throw new RuntimeException("Invalid ticket parameters: " + e.getMessage());
            }
        }
    }

    @Override
    protected Booking initializeBooking(BookingRequestDTO request) {
        User user = null;
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (email != null) {
            user = userRepository.findByEmail(email);
        }
        Booking booking = new Booking();
        booking.setPaymentStatus(PaymentStatusEnum.pending);
        booking.setContactEmail(user != null ? user.getEmail() : request.getContactEmail());
        booking.setContactPhone(request.getContactPhone());
        booking.setDate(Instant.now());
        booking.setPaymentType(request.getPaymentType());
        booking.setVnpTxnRef(booking.getBookingCode());
        booking.setUser(user);
        return booking;
    }

    @Override
    protected List<Seat> processSeats(BookingRequestDTO request, Booking booking) {
        TrainTrip trainTrip = trainTripRepository.findById(request.getTrainTripId())
                .orElseThrow(() -> new RuntimeException("TrainTrip not found: " + request.getTrainTripId()));
        Train train = trainTrip.getTrain();
        List<Seat> seats = seatRepository.findBySeatIdIn(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            logger.error("Requested seats: {}, Found seats: {}", request.getSeatIds().size(), seats.size());
            throw new RuntimeException("Một số ghế không tồn tại");
        }
        for (Seat seat : seats) {
            if (!seat.getCarriage().getTrain().equals(train)) {
                throw new RuntimeException("Ghế " + seat.getSeatId() + " không thuộc chuyến tàu của TrainTrip "
                        + request.getTrainTripId());
            }
        }
        return seats;
    }

    @Override
    protected List<Ticket> processTickets(BookingRequestDTO request, Booking booking, List<Seat> seats) {
        TrainTrip trainTrip = trainTripRepository.findById(request.getTrainTripId()).orElseThrow();
        Route route = trainTrip.getRoute();
        List<Station> allStations = new ArrayList<>();
        allStations.add(route.getOriginStation());
        allStations.addAll(route.getJourney());
        List<Map<String, Object>> ticketParams = null;
        if (request.getTicketsParam() != null && !request.getTicketsParam().isEmpty()) {
            try {
                ticketParams = objectMapper.readValue(request.getTicketsParam(), List.class);
            } catch (Exception e) {
                throw new RuntimeException("Invalid ticket parameters: " + e.getMessage());
            }
        }

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < request.getTickets().size(); i++) {
            TicketRequestDTO ticketDTO = request.getTickets().get(i);
            Seat seat = seats.get(i);
            Station boardingStation = stationRepository.findById(ticketDTO.getBoardingStationId())
                    .orElseThrow(() -> new RuntimeException("Ga lên tàu không tồn tại"));
            Station alightingStation = stationRepository.findById(ticketDTO.getAlightingStationId())
                    .orElseThrow(() -> new RuntimeException("Ga xuống tàu không tồn tại"));

            if (ticketParams != null) {
                Map<String, Object> ticketParam = ticketParams.get(i);
                Object boardingStationIdObj = ticketParam.get("boardingStationId");
                Object alightingStationIdObj = ticketParam.get("alightingStationId");
                Object seatNumberObj = ticketParam.get("seatNumber");
                Object priceObj = ticketParam.get("price");
                Long boardingStationIdFromParam = ((Number) boardingStationIdObj).longValue();
                Long alightingStationIdFromParam = ((Number) alightingStationIdObj).longValue();
                Long seatNumber = ((Number) seatNumberObj).longValue();
                Double priceFromParam = ((Number) priceObj).doubleValue();
                if (!boardingStationIdFromParam.equals(ticketDTO.getBoardingStationId()) ||
                        !alightingStationIdFromParam.equals(ticketDTO.getAlightingStationId())) {
                    throw new RuntimeException("boardingStationId or alightingStationId mismatch at index " + i);
                }
                if (seat.getSeatId() != seatNumber || seat.getPrice() != priceFromParam) {
                    throw new RuntimeException("Price or seat mismatch for seat " + seatNumber);
                }
            }

            if (!allStations.contains(boardingStation) || !allStations.contains(alightingStation)) {
                throw new RuntimeException("Ga lên hoặc xuống không thuộc lộ trình của chuyến tàu");
            }
            int boardingIndex = allStations.indexOf(boardingStation);
            int alightingIndex = allStations.indexOf(alightingStation);
            if (boardingIndex >= alightingIndex) {
                throw new RuntimeException("Ga lên tàu phải trước ga xuống tàu");
            }
            List<Seat> availableSeats = seatRepository.findAvailableSeatsForSegment(
                    request.getTrainTripId(), boardingIndex, alightingIndex);
            if (!availableSeats.contains(seat)) {
                throw new RuntimeException("Ghế " + seat.getSeatId() + " không khả dụng cho đoạn đường này");
            }

            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seat);
            ticket.setCustomerObject(ticketDTO.getCustomerObject());
            ticket.setName(ticketDTO.getName());
            ticket.setCitizenId(ticketDTO.getCitizenId());
            double basePrice = seat.getPrice();
            double discountMultiplier = getCustomerDiscountMultiplier(ticketDTO.getCustomerObject());
            ticket.setPrice(basePrice * discountMultiplier);
            ticket.setOwner(booking.getUser());
            ticket.setTicketStatus(TicketStatusEnum.issued);
            ticket.setBoardingOrder(boardingIndex);
            ticket.setAlightingOrder(alightingIndex);
            tickets.add(ticket);
        }
        return tickets;
    }

    @Override
    protected void applyPromotion(BookingRequestDTO request, Booking booking, List<Ticket> tickets) {
        double totalPrice = tickets.stream().mapToDouble(Ticket::getPrice).sum();
        logger.info("Total price before promotion: {}", totalPrice);
        if (request.getPromotionId() != null) {
            Promotion promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Promotion not found: " + request.getPromotionId()));
            if (promotion.getValidity().isBefore(Instant.now())) {
                throw new RuntimeException("Promotion " + promotion.getPromotionCode() + " has expired");
            }
            if (promotion.getStatus() != PromotionStatusEnum.active) {
                throw new RuntimeException("Promotion " + promotion.getPromotionCode() + " is not active");
            }
            totalPrice -= promotion.getDiscount();
            booking.setPromotion(promotion);
            logger.info("Applied promotion discount: {}", promotion.getDiscount());
        }
        booking.setTotalPrice(Math.max(0, totalPrice));
        logger.info("Total price after promotion: {}", totalPrice);
    }

    private double getCustomerDiscountMultiplier(CustomerObjectEnum customerObject) {
        if (customerObject == null)
            return 1.0;
        switch (customerObject) {
            case children:
                return 0.5;
            case student:
                return 0.85;
            case elderly:
                return 0.5;
            case veteran:
                return 0.0;
            case disabled:
                return 0.2;
            case adult:
            default:
                return 1.0;
        }
    }
}