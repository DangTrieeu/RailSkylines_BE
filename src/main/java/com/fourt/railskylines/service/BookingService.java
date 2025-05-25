package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.domain.response.*;
import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.template.BookingCreator;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingCreator bookingCreator;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public BookingService(
            @Qualifier("standardBookingCreator") BookingCreator bookingCreator, // Specify the bean
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            PaymentService paymentService,
            NotificationService notificationService,
            UserRepository userRepository) {
        this.bookingCreator = bookingCreator;
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(BookingRequestDTO request, HttpServletRequest httpServletRequest) {
        logger.info("Delegating booking creation to BookingCreator for trainTripId: {}", request.getTrainTripId());
        return bookingCreator.createBooking(request, httpServletRequest);
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

    public List<Booking> getBookingsByUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return bookingRepository.findByUser(user);
    }

    public List<ResBookingHistoryDTO> getBookingHistoryByUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        List<Booking> bookings = bookingRepository.findByUser(user);
        return bookings.stream().map(booking -> {
            ResBookingHistoryDTO dto = new ResBookingHistoryDTO();
            dto.setBookingCode(booking.getBookingCode());
            dto.setPaymentStatus(booking.getPaymentStatus());
            dto.setDate(booking.getDate());
            dto.setTotalPrice(booking.getTotalPrice());
            dto.setContactEmail(booking.getContactEmail());
            dto.setContactPhone(booking.getContactPhone());
            dto.setPaymentType(booking.getPaymentType());

            List<Ticket> tickets = booking.getTickets();
            if (tickets.isEmpty()) {
                logger.warn("Không tìm thấy vé cho booking: {}", booking.getBookingCode());
                return dto;
            }

            Ticket firstTicket = tickets.get(0);
            Seat seat = firstTicket.getSeat();
            Carriage carriage = seat.getCarriage();
            Train train = carriage.getTrain();
            List<TrainTrip> trainTrips = train.getTrip();

            TrainTrip trainTrip = trainTrips.stream()
                    .filter(trip -> {
                        List<Station> allStations = new ArrayList<>();
                        allStations.add(trip.getRoute().getOriginStation());
                        allStations.addAll(trip.getRoute().getJourney());
                        return firstTicket.getBoardingOrder() >= 0 &&
                                firstTicket.getBoardingOrder() < allStations.size() &&
                                firstTicket.getAlightingOrder() > firstTicket.getBoardingOrder() &&
                                firstTicket.getAlightingOrder() < allStations.size();
                    })
                    .findFirst()
                    .orElse(null);

            if (trainTrip == null) {
                logger.warn("Không tìm thấy TrainTrip cho vé: {}", firstTicket.getTicketCode());
                return dto;
            }

            Route route = trainTrip.getRoute();
            List<Station> allStations = new ArrayList<>();
            allStations.add(route.getOriginStation());
            allStations.addAll(route.getJourney());

            List<ResTicketHistoryDTO> ticketDtos = tickets.stream().map(ticket -> {
                ResTicketHistoryDTO ticketDto = new ResTicketHistoryDTO();
                ticketDto.setTicketCode(ticket.getTicketCode());
                ticketDto.setSeatId(ticket.getSeat().getSeatId());
                ticketDto.setPrice(ticket.getPrice());
                ticketDto.setName(ticket.getName());
                ticketDto.setCitizenId(ticket.getCitizenId());

                if (ticket.getBoardingOrder() >= 0 && ticket.getBoardingOrder() < allStations.size() &&
                        ticket.getAlightingOrder() >= 0 && ticket.getAlightingOrder() < allStations.size()) {
                    Station boardingStation = allStations.get(ticket.getBoardingOrder());
                    Station alightingStation = allStations.get(ticket.getAlightingOrder());
                    ticketDto.setBoardingStationName(boardingStation.getStationName());
                    ticketDto.setAlightingStationName(alightingStation.getStationName());
                } else {
                    logger.warn(
                            "boardingOrder hoặc alightingOrder không hợp lệ cho vé: {}, boardingOrder={}, alightingOrder={}",
                            ticket.getTicketCode(), ticket.getBoardingOrder(), ticket.getAlightingOrder());
                }

                ticketDto.setCarriageName(carriage.getCarriageType().toString());
                ticketDto.setTrainName(train.getTrainName());

                Schedule schedule = trainTrip.getSchedule();
                ticketDto.setStartDay(schedule.getDeparture().getDate());

                return ticketDto;
            }).collect(Collectors.toList());

            dto.setTickets(ticketDtos);
            return dto;
        }).collect(Collectors.toList());
    }

    public Booking findBookingByCodeAndVnpTxnRef(String bookingCode, String vnpTxnRef) {
        Booking booking = bookingRepository.findByBookingCodeAndVnpTxnRef(bookingCode, vnpTxnRef)
                .orElseThrow(
                        () -> new RuntimeException("Booking not found or VNP transaction reference does not match"));
        return booking;
    }

    public ResultPaginationDTO getAllBookings(Specification<Booking> spec, Pageable pageable) {
        Page<Booking> pageBookings = this.bookingRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageBookings.getTotalPages());
        meta.setTotal(pageBookings.getTotalElements());

        res.setMeta(meta);

        List<ResBookingDTO> listBookings = pageBookings.getContent()
                .stream()
                .map(this::convertToResBookingDTO)
                .collect(Collectors.toList());

        res.setResult(listBookings);
        return res;
    }

    public ResBookingDTO convertToResBookingDTO(Booking booking) {
        ResBookingDTO res = new ResBookingDTO();
        res.setBookingId(booking.getBookingId());
        res.setBookingCode(booking.getBookingCode());
        res.setDate(booking.getDate());
        res.setPaymentStatus(booking.getPaymentStatus());
        res.setTotalPrice(booking.getTotalPrice());
        res.setPayAt(booking.getPayAt());
        res.setTransactionId(booking.getTransactionId());
        res.setVnpTxnRef(booking.getVnpTxnRef());
        res.setPaymentType(booking.getPaymentType());
        res.setContactEmail(booking.getContactEmail());
        res.setContactPhone(booking.getContactPhone());
        res.setPromotion(booking.getPromotion());
        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            List<ResBookingDTO.ListTickets> listTickets = booking.getTickets().stream()
                    .map(ticket -> new ResBookingDTO.ListTickets(
                            ticket.getTicketCode(),
                            ticket.getCitizenId()))
                    .collect(Collectors.toList());
            res.setTickets(listTickets);
        }
        return res;
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findByBookingId(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    public BookingResponseDTO getBookingById(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findByVnpTxnRef(bookingId);
        if (bookingOpt.isEmpty()) {
            logger.error("Booking not found for booking ID: {}", bookingId);
            throw new RuntimeException("Booking not found for booking ID: " + bookingId);
        }
        Booking booking = bookingOpt.get();

        List<Ticket> tickets = this.ticketRepository.findByBooking(booking);
        logger.info("Fetched booking ID: {}, bookingCode: {}, ticket count: {}",
                bookingId, booking.getBookingCode(), tickets.size());

        BookingResponseDTO responseDTO = new BookingResponseDTO();
        responseDTO.setBookingId(booking.getBookingId());
        responseDTO.setBookingCode(booking.getBookingCode());
        responseDTO.setDate(booking.getDate());
        responseDTO.setPaymentStatus(booking.getPaymentStatus());
        responseDTO.setTotalPrice(booking.getTotalPrice());
        responseDTO.setPayAt(booking.getPayAt());
        responseDTO.setTransactionId(booking.getTransactionId());
        responseDTO.setVnpTxnRef(booking.getVnpTxnRef());
        responseDTO.setPaymentType(booking.getPaymentType());
        responseDTO.setContactEmail(booking.getContactEmail());
        responseDTO.setContactPhone(booking.getContactPhone());

        List<TicketResponseDTO> ticketDTOs = tickets.stream().map(ticket -> {
            TicketResponseDTO ticketDTO = new TicketResponseDTO();
            ticketDTO.setTicketId(ticket.getTicketId());
            ticketDTO.setCustomerObject(ticket.getCustomerObject());
            ticketDTO.setTicketCode(ticket.getTicketCode());
            ticketDTO.setName(ticket.getName());
            ticketDTO.setCitizenId(ticket.getCitizenId());
            ticketDTO.setPrice(ticket.getPrice());
            ticketDTO.setStartDay(ticket.getStartDay());
            ticketDTO.setTicketStatus(ticket.getTicketStatus());

            TicketResponseDTO.SeatDTO seatDTO = new TicketResponseDTO.SeatDTO();
            seatDTO.setSeatId(ticket.getSeat().getSeatId());
            seatDTO.setPrice(ticket.getSeat().getPrice());
            seatDTO.setSeatStatus(ticket.getSeat().getSeatStatus().name());
            ticketDTO.setSeat(seatDTO);

            TicketResponseDTO.TrainTripDTO trainTripDTO = new TicketResponseDTO.TrainTripDTO();
            TicketResponseDTO.TrainTripDTO.TrainDTO trainDTO = new TicketResponseDTO.TrainTripDTO.TrainDTO();
            trainTripDTO.setTrain(trainDTO);
            ticketDTO.setTrainTrip(trainTripDTO);
            return ticketDTO;
        }).collect(Collectors.toList());
        responseDTO.setTickets(ticketDTOs);

        return responseDTO;
    }
}