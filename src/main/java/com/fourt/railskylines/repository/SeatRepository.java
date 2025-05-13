// package com.fourt.railskylines.repository;

// import com.fourt.railskylines.domain.Carriage;
// import com.fourt.railskylines.domain.Seat;
// import com.fourt.railskylines.util.constant.SeatStatusEnum;
// import jakarta.persistence.LockModeType;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// import org.springframework.data.jpa.repository.Lock;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.util.List;

// @Repository
// public interface SeatRepository extends JpaRepository<Seat, Long>, JpaSpecificationExecutor<Seat> {
//     Page<Seat> findByCarriage_CarriageId(Long carriageId, Pageable pageable);

//     List<Seat> findByCarriageIn(List<Carriage> carriages);

//     List<Seat> findByCarriageCarriageId(Long carriageId);

//     void deleteByCarriage(Carriage carriage);

//     List<Seat> findBySeatIdInAndSeatStatus(List<Long> seatIds, SeatStatusEnum seatStatusEnum);

//     @Lock(LockModeType.PESSIMISTIC_WRITE)
//     @Query("SELECT s FROM Seat s WHERE s.seatId IN :seatIds")
//     List<Seat> findBySeatIdIn(@Param("seatIds") List<Long> seatIds);

//     @Query("SELECT s FROM Seat s " +
//             "WHERE s.carriage.train IN (SELECT t.train FROM TrainTrip t WHERE t.id = :trainTripId) " +
//             "AND NOT EXISTS (" +
//             "SELECT t FROM Ticket t " +
//             "WHERE t.seat = s AND t.ticketStatus IN ('issued', 'used') " +
//             "AND t.boardingOrder < :alightingOrder AND t.alightingOrder > :boardingOrder" +
//             ")")
//     List<Seat> findAvailableSeatsForSegment(@Param("trainTripId") Long trainTripId,
//             @Param("boardingOrder") int boardingOrder,
//             @Param("alightingOrder") int alightingOrder);

//     void deleteByCarriageCarriageId(Long id);
// }

package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Carriage;
import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, JpaSpecificationExecutor<Seat> {
        Page<Seat> findByCarriage_CarriageId(Long carriageId, Pageable pageable);

        List<Seat> findByCarriageIn(List<Carriage> carriages);

        List<Seat> findByCarriageCarriageId(Long carriageId);

        void deleteByCarriage(Carriage carriage);

        List<Seat> findBySeatIdInAndSeatStatus(List<Long> seatIds, SeatStatusEnum seatStatusEnum);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM Seat s WHERE s.seatId IN :seatIds")
        List<Seat> findBySeatIdIn(@Param("seatIds") List<Long> seatIds);

        @Query("SELECT s FROM Seat s " +
                        "WHERE s.carriage.train IN (SELECT t.train FROM TrainTrip t WHERE t.id = :trainTripId) " +
                        "AND NOT EXISTS (" +
                        "SELECT t FROM Ticket t " +
                        "WHERE t.seat = s AND t.ticketStatus IN ('issued', 'used') " +
                        "AND t.boardingOrder < :alightingOrder AND t.alightingOrder > :boardingOrder" +
                        ")")
        List<Seat> findAvailableSeatsForSegment(@Param("trainTripId") Long trainTripId,
                        @Param("boardingOrder") int boardingOrder,
                        @Param("alightingOrder") int alightingOrder);

        void deleteByCarriageCarriageId(Long id);
}
