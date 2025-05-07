package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        Optional<Booking> findByBookingCode(String bookingCode);

        List<Booking> findByUser(User user);

        @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.paymentStatus = :status " +
                        "AND (:startDate IS NULL OR b.date >= :startDate) " +
                        "AND (:endDate IS NULL OR b.date <= :endDate)")
        Double sumTotalPriceByPaymentStatusAndDateRange(
                        @Param("status") PaymentStatusEnum status,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("SELECT b FROM Booking b WHERE " +
                        "(:startDate IS NULL OR b.date >= :startDate) AND " +
                        "(:endDate IS NULL OR b.date <= :endDate) AND " +
                        "b.paymentStatus IN :statuses")
        List<Booking> findByDateBetweenAndPaymentStatusIn(
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate,
                        @Param("statuses") List<PaymentStatusEnum> statuses);
}