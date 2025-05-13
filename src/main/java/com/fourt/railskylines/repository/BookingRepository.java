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
    List<Booking> findByUser(User user);

    Optional<Booking> findByBookingCode(String bookingCode);

    Optional<Booking> findByVnpTxnRef(String vnpTxnRef);

    List<Booking> findByPaymentStatusAndDateBefore(PaymentStatusEnum paymentStatus, Instant date);

    List<Booking> findByPaymentStatusNotAndDateBefore(PaymentStatusEnum paymentStatus, Instant date);

    @Query("SELECT b FROM Booking b JOIN FETCH b.tickets WHERE b.bookingId = :bookingId")
    Optional<Booking> findByBookingIdWithTickets(@Param("bookingId") Long bookingId);

    Optional<Booking> findByBookingId(Long bookingId);

    Optional<Booking> findByBookingCodeAndVnpTxnRef(String bookingCode, String vnpTxnRef);

    List<Booking> findByDateBetweenAndPaymentStatusIn(Instant startDate, Instant endDate, List<PaymentStatusEnum> statuses);
}