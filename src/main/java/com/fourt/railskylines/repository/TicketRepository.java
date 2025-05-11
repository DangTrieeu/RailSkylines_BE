package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.domain.TrainTrip;
import com.fourt.railskylines.util.constant.TicketStatusEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Ticket t " +
            "WHERE t.seat = :seat AND t.trainTrip = :trainTrip AND t.ticketStatus IN :statuses " +
            "AND t.boardingOrder < :desiredAlightingOrder AND t.alightingOrder > :desiredBoardingOrder")
    boolean hasOverlappingTicket(@Param("seat") Seat seat, 
                                 @Param("trainTrip") TrainTrip trainTrip, 
                                 @Param("statuses") List<TicketStatusEnum> statuses,
                                 @Param("desiredBoardingOrder") int desiredBoardingOrder, 
                                 @Param("desiredAlightingOrder") int desiredAlightingOrder);

    Optional<Ticket> findByTicketCodeAndCitizenId(String ticketCode, String citizenId);
}