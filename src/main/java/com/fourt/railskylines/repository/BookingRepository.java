package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        Optional<Booking> findByBookingCode(String bookingCode);

        List<Booking> findByUser(User user);

        Optional<Booking> findByBookingCode(String bookingCode);

}