package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.response.ResRevenueDTO;
import com.fourt.railskylines.repository.BookingRepository;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RevenueService {

        private static final Logger logger = LoggerFactory.getLogger(RevenueService.class);
        private final BookingRepository bookingRepository;
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withZone(ZoneId.systemDefault());
        private static final String[] colors = {
                        "var(--color-chrome)", "var(--color-safari)", "var(--color-firefox)",
                        "var(--color-edge)", "var(--color-other)"
        };

        public RevenueService(BookingRepository bookingRepository) {
                this.bookingRepository = bookingRepository;
        }

        public ResRevenueDTO calculateRevenueData(Instant startDate, Instant endDate) {
                logger.info("Tính toán dữ liệu doanh thu từ {} đến {}", startDate, endDate);
                ResRevenueDTO dto = new ResRevenueDTO();

                // Lấy tất cả booking trong khoảng thời gian
                List<Booking> bookings = bookingRepository.findByDateBetweenAndPaymentStatusIn(
                                startDate, endDate, List.of(PaymentStatusEnum.success, PaymentStatusEnum.pending));

                // Tính tổng doanh thu và số vé đã thanh toán
                double totalRevenue = bookings.stream()
                                .filter(b -> b.getPaymentStatus() == PaymentStatusEnum.success)
                                .mapToDouble(Booking::getTotalPrice)
                                .sum();
                long paidTickets = bookings.stream()
                                .filter(b -> b.getPaymentStatus() == PaymentStatusEnum.success)
                                .mapToLong(b -> b.getTickets().size())
                                .sum();
                long pendingTickets = bookings.stream()
                                .filter(b -> b.getPaymentStatus() == PaymentStatusEnum.pending)
                                .mapToLong(b -> b.getTickets().size())
                                .sum();
                long totalCustomers = bookings.stream()
                                .flatMap(b -> b.getTickets().stream())
                                .filter(t -> t.getSeat() != null) // Đảm bảo ticket có seat
                                .count(); // Tổng số ghế đã đặt (từ vé của booking success hoặc pending)

                // Tính doanh thu theo ngày
                Map<String, Double> revenueByDateMap = bookings.stream()
                                .filter(b -> b.getPaymentStatus() == PaymentStatusEnum.success)
                                .collect(Collectors.groupingBy(
                                                b -> DATE_FORMATTER.format(b.getDate()),
                                                Collectors.summingDouble(Booking::getTotalPrice)));
                List<ResRevenueDTO.RevenueByDate> revenueByDate = revenueByDateMap.entrySet().stream()
                                .map(entry -> {
                                        ResRevenueDTO.RevenueByDate rbd = new ResRevenueDTO.RevenueByDate();
                                        rbd.setDate(entry.getKey());
                                        rbd.setRevenue(entry.getValue());
                                        return rbd;
                                })
                                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                                .collect(Collectors.toList());

                // Tính xếp hạng tàu
                Map<String, Long> trainTicketCounts = bookings.stream()
                                .filter(b -> b.getPaymentStatus() == PaymentStatusEnum.success)
                                .flatMap(b -> b.getTickets().stream())
                                .collect(Collectors.groupingBy(
                                                t -> t.getTrainTrip().getTrain().getTrainName(),
                                                Collectors.counting()));
                List<ResRevenueDTO.TrainRanking> trainRankings = trainTicketCounts.entrySet().stream()
                                .map(entry -> {
                                        ResRevenueDTO.TrainRanking tr = new ResRevenueDTO.TrainRanking();
                                        tr.setName(entry.getKey());
                                        tr.setSuccessOrders(entry.getValue());
                                        tr.setFill(colors[trainTicketCounts.size() % colors.length]); // Gán màu tuần tự
                                        return tr;
                                })
                                .sorted((a, b) -> Long.compare(b.getSuccessOrders(), a.getSuccessOrders()))
                                .collect(Collectors.toList());

                // Gán giá trị cho DTO
                dto.setTotalRevenue(totalRevenue);
                dto.setTotalCustomers(totalCustomers);
                dto.setPaidTickets(paidTickets);
                dto.setPendingTickets(pendingTickets);
                dto.setRevenueByDate(revenueByDate);
                dto.setTrainRankings(trainRankings);

                logger.info("Dữ liệu doanh thu đã tính: tổng doanh thu = {}, số ghế đã đặt = {}, vé đã thanh toán = {}, vé đang đặt = {}",
                                totalRevenue, totalCustomers, paidTickets, pendingTickets);
                return dto;
        }
}