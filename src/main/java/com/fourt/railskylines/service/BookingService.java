package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.domain.request.TicketRequestDTO;
import com.fourt.railskylines.domain.response.PaymentDTO;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import com.fourt.railskylines.util.VNPayUtil;
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

    public BookingService(SeatRepository seatRepository, BookingRepository bookingRepository,
            TicketRepository ticketRepository, PromotionRepository promotionRepository,
            UserRepository userRepository, NotificationService notificationService,
            PaymentService paymentService, ObjectMapper objectMapper) {
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO request, HttpServletRequest httpServletRequest) {
        // 1. Kiểm tra ghế
        List<Seat> seats = seatRepository.findBySeatIdInAndSeatStatus(request.getSeatIds(), SeatStatusEnum.available);
        if (seats.size() != request.getSeatIds().size()) {
            logger.error("Requested seats: {}, Available seats: {}", request.getSeatIds().size(), seats.size());
            throw new RuntimeException("Một số ghế không khả dụng");
        }

        // Kiểm tra giá từ tickets param (nếu có)
        if (request.getTicketsParam() != null && !request.getTicketsParam().isEmpty()) {
            try {
                logger.info("Parsing ticketsParam: {}", request.getTicketsParam());
                List<Map<String, Object>> ticketParams = objectMapper.readValue(request.getTicketsParam(), List.class);
                for (int i = 0; i < seats.size(); i++) {
                    Map<String, Object> ticketParam = ticketParams.get(i);
                    Object seatNumberObj = ticketParam.get("seatNumber");
                    Object priceObj = ticketParam.get("price");

                    if (!(seatNumberObj instanceof Number)) {
                        throw new RuntimeException(
                                "Invalid seatNumber for ticket at index " + i + ": " + seatNumberObj);
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
            } catch (Exception e) {
                logger.error("Error parsing ticket params: {}", e.getMessage(), e);
                throw new RuntimeException("Invalid ticket parameters: " + e.getMessage());
            }
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
        booking.setPaymentType(request.getPaymentType());
        booking.setVnpTxnRef(booking.getBookingCode());

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

        // 5. Áp dụng khuyến mãi
        double totalPrice = tickets.stream().mapToDouble(Ticket::getPrice).sum();
        logger.info("Total price before discount: {}", totalPrice);
        if (request.getPromotionIds() != null && !request.getPromotionIds().isEmpty()) {
            List<Promotion> promotions = promotionRepository.findByPromotionIdIn(request.getPromotionIds());
            if (promotions.size() != request.getPromotionIds().size()) {
                logger.warn("Some promotions not found: requested {}, found {}", request.getPromotionIds().size(),
                        promotions.size());
            }
            double discount = promotions.stream().mapToDouble(Promotion::getDiscount).sum();
            logger.info("Applied discount: {}", discount);
            totalPrice -= discount;
            booking.setPromotions(promotions);
        }
        if (totalPrice < 0)
            totalPrice = 0;
        booking.setTotalPrice(totalPrice);
        logger.info("Total price after discount: {}", totalPrice);

        // 6. Lưu tạm thời
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
            seats.forEach(seat -> seat.setSeatStatus(SeatStatusEnum.booked));
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
            seats.forEach(seat -> seat.setSeatStatus(SeatStatusEnum.available));
            ticketRepository.deleteAll(booking.getTickets());
            booking.getTickets().clear();
            logger.warn("Payment failed for booking code: {}", booking.getBookingCode());
        }

        seatRepository.saveAll(seats);
        bookingRepository.save(booking);
    }

    @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút
    @Transactional
    public void cleanupFailedBookings() {
        Instant fifteenMinutesAgo = Instant.now().minusSeconds(15 * 60); // 15 phút trước
        List<Booking> failedBookings = bookingRepository.findByPaymentStatusAndDateBefore(
                PaymentStatusEnum.pending, fifteenMinutesAgo);

        for (Booking booking : failedBookings) {
            logger.info("Cleaning up failed booking: {}", booking.getBookingCode());

            // Tải lại booking với tickets để đảm bảo chúng được quản lý
            Booking managedBooking = bookingRepository.findById(booking.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + booking.getBookingCode()));

            // Chuyển ghế về available
            List<Seat> seats = managedBooking.getTickets().stream()
                    .map(Ticket::getSeat)
                    .filter(seat -> seat != null)
                    .toList();
            seats.forEach(seat -> seat.setSeatStatus(SeatStatusEnum.available));
            seatRepository.saveAll(seats);

            // Xóa tickets
            ticketRepository.deleteAll(managedBooking.getTickets());
            managedBooking.getTickets().clear();

            // Xóa booking
            bookingRepository.delete(managedBooking);
            logger.info("Deleted failed booking: {}", managedBooking.getBookingCode());
        }
    }

    public List<Booking> getBookingsByUser(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return bookingRepository.findByUser(user);
    }
}