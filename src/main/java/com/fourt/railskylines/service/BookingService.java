package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.domain.request.TicketRequestDTO;
import com.fourt.railskylines.domain.response.PaymentResponse;
import com.fourt.railskylines.integration.PaymentGateway;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    public BookingService(SeatRepository seatRepository, BookingRepository bookingRepository,
                          TicketRepository ticketRepository, PromotionRepository promotionRepository,
                          UserRepository userRepository, PaymentGateway paymentGateway,
                          NotificationService notificationService) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        // 1. Kiểm tra ghế
        List<Seat> seats = seatRepository.findBySeatIdInAndSeatStatus(request.getSeatIds(), SeatStatusEnum.available);
        if (seats.size() != request.getSeatIds().size()) {
            logger.error("Requested seats: {}, Available seats: {}", request.getSeatIds().size(), seats.size());
            throw new RuntimeException("Một số ghế không khả dụng");
        }

        // 2. Khóa ghế
        seats.forEach(seat -> seat.setSeatStatus(SeatStatusEnum.pending));
        seatRepository.saveAll(seats);

        // 3. Tạo Booking
        Booking booking = new Booking();
        booking.setPaymentStatus(PaymentStatusEnum.pending);
        booking.setContactEmail(request.getContactEmail());
        booking.setContactPhone(request.getContactPhone());
        booking.setDate(Instant.now());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            booking.setUser(user);
        }
        booking = bookingRepository.save(booking);

        // 4. Tạo Ticket
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < request.getTickets().size(); i++) {
            TicketRequestDTO ticketDTO = request.getTickets().get(i);
            Ticket ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setSeat(seats.get(i));
            ticket.setCustomerObject(ticketDTO.getCustomerObject());
            ticket.setName(ticketDTO.getName());
            ticket.setCitizenId(ticketDTO.getCitizenId());
            ticket.setPrice(seats.get(i).getPrice());
            ticket.setTrainTrip(seats.get(i).getTrainTrip());
            ticket.setOwner(booking.getUser());
            ticket.setTicketStatus(TicketStatusEnum.issued);
            tickets.add(ticket);
        }
        ticketRepository.saveAll(tickets);
        booking.setTickets(tickets);

        // 5. Áp dụng khuyến mãi
        double totalPrice = tickets.stream().mapToDouble(Ticket::getPrice).sum();
        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            List<Promotion> promotions = promotionRepository.findByPromotionIdIn(request.getPromotionIds());
            if (promotions.size() != request.getPromotionIds().size()) {
                logger.warn("Some promotions not found: requested {}, found {}", request.getPromotionIds().size(), promotions.size());
            }
            double discount = promotions.stream().mapToDouble(Promotion::getDiscount).sum();
            totalPrice -= discount;
            booking.setPromotions(promotions);
        }
        booking.setTotalPrice(totalPrice);

        // 6. Lưu booking
        bookingRepository.save(booking);

        logger.info("Booking created successfully with code: {}", booking.getBookingCode());
        return booking;
    }

    public PaymentResponse getPaymentResponse(double amount, String bookingId, HttpServletRequest request) {
        PaymentResponse paymentResponse = paymentGateway.processPayment(amount, bookingId, request);
        if (!paymentResponse.isSuccess()) {
            logger.error("Failed to generate payment URL for booking {}: {}", bookingId, paymentResponse.getMessage());
            throw new RuntimeException("Failed to generate payment URL: " + paymentResponse.getMessage());
        }
        logger.info("Payment URL generated successfully for booking {}: {}", bookingId, paymentResponse.getPaymentUrl());
        return paymentResponse;
    }

    @Transactional
    public void updateBookingPaymentStatus(String txnRef, boolean success, String transactionNo) {
        Optional<Booking> bookingOpt = bookingRepository.findByVnpTxnRef(txnRef);
        if (bookingOpt.isEmpty()) {
            logger.error("Booking not found for VNPay transaction reference: {}", txnRef);
            throw new RuntimeException("Booking not found for transaction reference: " + txnRef);
        }

        Booking booking = bookingOpt.get();
        List<Seat> seats = booking.getTickets().stream().map(Ticket::getSeat).toList();
        if (success) {
            booking.setPaymentStatus(PaymentStatusEnum.success);
            booking.setPayAt(Instant.now());
            booking.setTransactionId(transactionNo);
            seats.forEach(seat -> seat.setSeatStatus(SeatStatusEnum.unavailable));
            booking.getTickets().forEach(ticket -> ticket.setTicketStatus(TicketStatusEnum.used));
            try {
                notificationService.sendBookingConfirmation(booking, booking.getTickets());
                logger.info("Booking confirmed and email sent for booking code: {}", booking.getBookingCode());
            } catch (MessagingException e) {
                logger.error("Failed to send confirmation email for booking code {}: {}", booking.getBookingCode(), e.getMessage());
            }
        } else {
            booking.setPaymentStatus(PaymentStatusEnum.failed);
            seats.forEach(seat -> seat.setSeatStatus(SeatStatusEnum.available));
            booking.getTickets().forEach(ticket -> ticket.setTicketStatus(TicketStatusEnum.cancelled));
            logger.warn("Payment failed for booking code: {}", booking.getBookingCode());
        }
        seatRepository.saveAll(seats);
        ticketRepository.saveAll(booking.getTickets());
        bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByUser(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return bookingRepository.findByUser(user);
    }

    public Booking findBookingByTxnRef(String txnRef) {
        return bookingRepository.findByVnpTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Booking not found for transaction reference: " + txnRef));
    }

    @Transactional
    public void updateBooking(Booking booking) {
        bookingRepository.save(booking);
    }
}