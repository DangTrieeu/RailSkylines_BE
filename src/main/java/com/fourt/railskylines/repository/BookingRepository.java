package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

    Optional<Booking> findByBookingCode(String bookingCode);

    Optional<Booking> findByVnpTxnRef(String vnpTxnRef);

    List<Booking> findByPaymentStatusAndDateBefore(PaymentStatusEnum paymentStatus, Instant date);
}