package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Optional<Ticket> findByTicketCodeAndCitizenId(String ticketCode, String citizenId);

    boolean existsBySeatInAndTicketStatusIn(List<Seat> seats, List<TicketStatusEnum> statuses);
}