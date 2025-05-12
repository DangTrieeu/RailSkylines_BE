package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.util.constant.TicketStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketCodeAndCitizenId(String ticketCode, String citizenId);

    List<Ticket> findByBooking(Booking booking);

    Optional<Ticket> findByTicketCode(String ticketCode);

    boolean existsBySeatInAndTicketStatusIn(List<Seat> seats, List<TicketStatusEnum> statuses);

}
