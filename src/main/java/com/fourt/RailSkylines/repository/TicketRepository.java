package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fourt.RailSkylines.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
