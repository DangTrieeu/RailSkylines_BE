package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.domain.request.TicketRequestDTO;
import com.fourt.railskylines.domain.response.PaymentDTO;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.util.constant.CustomerObjectEnum;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import com.fourt.railskylines.util.constant.PromotionStatusEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.round;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final StationRepository stationRepository;

    public BookingService(SeatRepository seatRepository, BookingRepository bookingRepository,
            TicketRepository ticketRepository, PromotionRepository promotionRepository,
            UserRepository userRepository, NotificationService notificationService,
            PaymentService paymentService, ObjectMapper objectMapper,
            StationRepository stationRepository) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.stationRepository = stationRepository;
    }

    private double getCustomerDiscountMultiplier(CustomerObjectEnum customerObject) {
        if (customerObject == null) {
            return 1.0;
        }
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

    @Transactional
    public Booking createBooking(BookingRequestDTO request, HttpServletRequest httpServletRequest) {
        List<Seat> seats = seatRepository.findBySeatIdIn(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            logger.error("Requested seats: {}, Found seats: {}", request.getSeatIds().size(), seats.size());
            throw new RuntimeException("Một số ghế không tồn tại");
        }

        Set<TrainTrip> trainTrips = seats.stream().map(Seat::getTrainTrip).collect(Collectors.toSet());
        if (trainTrips.size() > 1) {
            throw new RuntimeException("Tất cả các ghế phải thuộc cùng một chuyến tàu");
        }
        TrainTrip trainTrip = trainTrips.iterator().next();
        Route route = trainTrip.getRoute();

        List<Map<String, Object>> ticketParams = null;
        if (request.getTicketsParam() != null && !request.getTicketsParam().isEmpty()) {
            try {
                logger.info("Parsing ticketsParam: {}", request.getTicketsParam());
                ticketParams = objectMapper.readValue(request.getTicketsParam(), List.class);
                if (ticketParams.size() != request.getTickets().size()) {
                    throw new RuntimeException("Số lượng vé trong ticketsParam không khớp với số lượng vé trong body");
                }
            } catch (Exception e) {
                logger.error("Error parsing ticket params: {}", e.getMessage(), e);
                throw new RuntimeException("Invalid ticket parameters: " + e.getMessage());
            }
        }

        for (int i = 0; i < request.getTickets().size(); i++) {
            TicketRequestDTO ticketDTO = request.getTickets().get(i);
            Seat seat = seats.get(i);

            Optional<Station> boardingStationOpt = stationRepository.findById(ticketDTO.getBoardingStationId());
            Optional<Station> alightingStationOpt = stationRepository.findById(ticketDTO.getAlightingStationId());
            if (boardingStationOpt.isEmpty()) {
                throw new RuntimeException("Ga lên tàu không tồn tại");
            }
            if (alightingStationOpt.isEmpty()) {
                throw new RuntimeException("Ga xuống tàu không tồn tại");
            }
            Station boardingStation = boardingStationOpt.get();
            Station alightingStation = alightingStationOpt.get();

            // Kiểm tra đồng bộ với ticketsParam
            if (ticketParams != null) {
                Map<String, Object> ticketParam = ticketParams.get(i);
                Object boardingStationIdObj = ticketParam.get("boardingStationId");
                Object alightingStationIdObj = ticketParam.get("alightingStationId");

                if (!(boardingStationIdObj instanceof Number) || !(alightingStationIdObj instanceof Number)) {
                    throw new RuntimeException("Invalid boardingStationId or alightingStationId in ticketsParam at index " + i);
                }

                Long boardingStationIdFromParam = ((Number) boardingStationIdObj).longValue();
                Long alightingStationIdFromParam = ((Number) alightingStationIdObj).longValue();

                if (!boardingStationIdFromParam.equals(ticketDTO.getBoardingStationId()) ||
                    !alightingStationIdFromParam.equals(ticketDTO.getAlightingStationId())) {
                    throw new RuntimeException("boardingStationId or alightingStationId mismatch between ticketsParam and body at index " + i);
                }
            }

            List<Station> routeStations = route.getJourney();
            List<Long> stationIdsInRoute = routeStations.stream()
                    .map(Station::getStationId)
                    .collect(Collectors.toList());
            if (!stationIdsInRoute.contains(boardingStation.getStationId()) ||
                !stationIdsInRoute.contains(alightingStation.getStationId())) {
                throw new RuntimeException("Ga lên hoặc xuống không thuộc lộ trình của chuyến tàu");
            }

            // Sử dụng position từ stations và làm tròn để lấy boardingOrder và alightingOrder
            int boardingOrder = (int) round(boardingStation.getPosition());
            int alightingOrder = (int) round(alightingStation.getPosition());

            if (boardingOrder >= alightingOrder) {
                throw new RuntimeException("Ga lên tàu phải trước ga xuống tàu");
            }

            if (ticketRepository.hasOverlappingTicket(seat, trainTrip,
                    List.of(TicketStatusEnum.issued, TicketStatusEnum.used),
                    boardingOrder, alightingOrder)) {
                throw new RuntimeException("Ghế không khả dụng cho đoạn đường này");
            }
        }

        if (ticketParams != null) {
            for (int i = 0; i < seats.size(); i++) {
                Map<String, Object> ticketParam = ticketParams.get(i);
                Object seatNumberObj = ticketParam.get("seatNumber");
                Object priceObj = ticketParam.get("price");

                if (!(seatNumberObj instanceof Number)) {
                    throw new RuntimeException("Invalid seatNumber for ticket at index " + i + ": " + seatNumberObj);
                }
                if (!(priceObj instanceof Number)) {
                    throw new RuntimeException("Invalid price for ticket at index " + i + ": " + priceObj);
                }

                Long seatNumber = ((Number) seatNumberObj).longValue();
                Double priceFromParam = ((Number) priceObj).doubleValue();

                logger.info("Checking seat: dbSeatId={}, dbPrice={}, paramSeatNumber={}, paramPrice={}",
                        seats.get(i).getSeatId(), seats.get(i).getPrice(), seatNumber, priceFromParam);

                if (seats.get(i).getSeatId() != seatNumber || seats.get(i).getPrice() != priceFromParam) {
                    throw new RuntimeException("Price or seat mismatch for seat " + seatNumber);
                }
            }
        }

        Booking booking = new Booking();
        booking.setPaymentStatus(PaymentStatusEnum.pending);
        booking.setContactEmail(request.getContactEmail());
        booking.setContactPhone(request.getContactPhone());
        booking.setDate(Instant.now());
        booking.setPaymentType(request.getPaymentType());
        booking.setVnpTxnRef(booking.getBookingCode());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            booking.setUser(user);
        }
        booking = bookingRepository.save(booking);

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < request.getTickets().size(); i++) {
            TicketRequestDTO ticketDTO = request.getTickets().get(i);
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seats.get(i));
            ticket.setCustomerObject(ticketDTO.getCustomerObject());
            ticket.setName(ticketDTO.getName());
            ticket.setCitizenId(ticketDTO.getCitizenId());
            double basePrice = seats.get(i).getPrice();
            double discountMultiplier = getCustomerDiscountMultiplier(ticketDTO.getCustomerObject());
            double discountedPrice = basePrice * discountMultiplier;
            ticket.setPrice(discountedPrice);
            logger.info("Ticket for {}: basePrice={}, discountMultiplier={}, discountedPrice={}",
                    ticketDTO.getName(), basePrice, discountMultiplier, discountedPrice);
            ticket.setTrainTrip(seats.get(i).getTrainTrip());
            ticket.setOwner(booking.getUser());
            ticket.setTicketStatus(TicketStatusEnum.issued);
            Station boardingStation = stationRepository.findById(ticketDTO.getBoardingStationId()).orElseThrow();
            Station alightingStation = stationRepository.findById(ticketDTO.getAlightingStationId()).orElseThrow();
            ticket.setBoardingStation(boardingStation);
            ticket.setAlightingStation(alightingStation);
            ticket.setBoardingOrder((int) round(boardingStation.getPosition()));
            ticket.setAlightingOrder((int) round(alightingStation.getPosition()));
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);

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
            double discount = promotion.getDiscount();
            logger.info("Applied promotion discount: {}", discount);
            totalPrice -= discount;
            booking.setPromotion(promotion);
        }
        if (totalPrice < 0)
            totalPrice = 0;
        booking.setTotalPrice(totalPrice);
        logger.info("Total price after promotion: {}", totalPrice);

        bookingRepository.save(booking);

        logger.info("Booking created successfully with code: {}", booking.getBookingCode());
        return booking;
    }

    public String getPaymentUrl(Booking booking, HttpServletRequest httpServletRequest) {
        long amount = (long) booking.getTotalPrice();
        String bankCode = "NCB";
        httpServletRequest.setAttribute("txnRef", booking.getBookingCode());

        PaymentDTO.VNPayResponse vnPayResponse = paymentService.createVnPayPayment(httpServletRequest, amount, bankCode,
                booking.getBookingCode());

        if (!"ok".equals(vnPayResponse.getCode())) {
            logger.error("Failed to generate payment URL: {}", vnPayResponse.getMessage());
            throw new RuntimeException("Failed to generate payment URL: " + vnPayResponse.getMessage());
        }

        String paymentUrl = vnPayResponse.getPaymentUrl();
        booking.setVnpTxnRef(booking.getBookingCode());
        bookingRepository.save(booking);

        logger.info("Payment URL generated successfully: {}", paymentUrl);
        return paymentUrl;
    }

    @Transactional
    public void updateBookingPaymentStatus(String txnRef, boolean success, String transactionNo) {
        Optional<Booking> bookingOpt = bookingRepository.findByVnpTxnRef(txnRef);
        if (bookingOpt.isEmpty()) {
            logger.error("Booking not found for transaction reference: {}", txnRef);
            throw new RuntimeException("Booking not found for transaction reference: " + txnRef);
        }

        Booking booking = bookingOpt.get();
        List<Seat> seats = booking.getTickets().stream().map(Ticket::getSeat).toList();

        if (success) {
            booking.setPaymentStatus(PaymentStatusEnum.success);
            booking.setPayAt(Instant.now());
            booking.setTransactionId(transactionNo);
            booking.getTickets().forEach(ticket -> ticket.setTicketStatus(TicketStatusEnum.used));
            try {
                notificationService.sendBookingConfirmation(booking, booking.getTickets());
                logger.info("Booking confirmed and email sent for booking code: {}", booking.getBookingCode());
            } catch (MessagingException e) {
                logger.error("Failed to send confirmation email for booking code {}: {}", booking.getBookingCode(),
                        e.getMessage());
            }
        } else {
            booking.setPaymentStatus(PaymentStatusEnum.failed);
            booking.getTickets().forEach(ticket -> ticket.setTicketStatus(TicketStatusEnum.cancelled));
            logger.warn("Payment failed for booking code: {}", booking.getBookingCode());
        }

        ticketRepository.saveAll(booking.getTickets());
        bookingRepository.save(booking);
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupFailedBookings() {
        logger.info("Start cleaning failed bookings at {}", Instant.now());
        Instant fifteenMinutesAgo = Instant.now().minusSeconds(15 * 60);

        List<Booking> failedBookings = bookingRepository.findByPaymentStatusNotAndDateBefore(
                PaymentStatusEnum.success, fifteenMinutesAgo);

        logger.info("Found {} failed bookings", failedBookings.size());
        for (Booking booking : failedBookings) {
            try {
                cleanupSingleBooking(booking.getBookingId());
            } catch (Exception e) {
                logger.error("Error cleaning booking {}: {}", booking.getBookingCode(), e.getMessage());
            }
        }
        logger.info("Finished cleaning failed bookings at {}", Instant.now());
    }

    @Transactional
    public void cleanupSingleBooking(Long bookingId) {
        logger.info("Cleaning booking ID: {}", bookingId);

        Booking managedBooking = bookingRepository.findByBookingIdWithTickets(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking ID not found: " + bookingId));

        List<Ticket> ticketsToUpdate = managedBooking.getTickets().stream()
                .filter(ticket -> !TicketStatusEnum.used.equals(ticket.getTicketStatus()))
                .toList();

        if (!ticketsToUpdate.isEmpty()) {
            ticketsToUpdate.forEach(ticket -> {
                logger.info("Updating ticket {} to CANCELLED", ticket.getTicketCode());
                ticket.setTicketStatus(TicketStatusEnum.cancelled);
            });
            ticketRepository.saveAll(ticketsToUpdate);
            ticketRepository.flush();
        }

        if (!PaymentStatusEnum.failed.equals(managedBooking.getPaymentStatus())) {
            logger.info("Updating booking {} to FAILED", managedBooking.getBookingCode());
            managedBooking.setPaymentStatus(PaymentStatusEnum.failed);
            bookingRepository.save(managedBooking);
            bookingRepository.flush();
        }

        logger.info("Cleaned up booking: {}", managedBooking.getBookingCode());
    }

    public List<Booking> getBookingsByUser(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return bookingRepository.findByUser(user);
    }
}