package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketCodeAndCitizenId(String ticketCode, String citizenId);
}