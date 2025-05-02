package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Seat;
import com.fourt.railskylines.domain.dto.ResultPaginationDTO;
import com.fourt.railskylines.repository.CarriageRepository;
import com.fourt.railskylines.repository.SeatRepository;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.SeatTypeEnum;
import com.fourt.railskylines.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final CarriageRepository carriageRepository;

    public SeatService(SeatRepository seatRepository, CarriageRepository carriageRepository) {
        this.seatRepository = seatRepository;
        this.carriageRepository = carriageRepository;
    }

    @Transactional
    public Seat handleCreateSeat(Seat seat) throws IdInvalidException {
        // Validate carriage
        if (seat.getCarriage() == null || seat.getCarriage().getCarriageId() <= 0) {
            throw new IdInvalidException("Carriage ID must be provided and valid");
        }

        // Check if carriage exists
        Long carriageId = seat.getCarriage().getCarriageId();
        if (!carriageRepository.existsById(carriageId)) {
            throw new IdInvalidException("Carriage with ID " + carriageId + " does not exist");
        }

        // Validate seat status
        if (seat.getSeatStatus() == null) {
            throw new IdInvalidException("Seat status must be provided");
        }

        // Validate seat type
        if (seat.getSeatType() == null) {
            throw new IdInvalidException("Seat type must be provided");
        }

        // Validate price
        if (seat.getPrice() <= 0) {
            throw new IdInvalidException("Seat price must be greater than zero");
        }

        // Save seat
        return seatRepository.save(seat);
    }

    public Seat fetchSeatById(Long id) {
        Optional<Seat> seatOptional = seatRepository.findById(id);
        return seatOptional.orElse(null);
    }

    public ResultPaginationDTO fetchAllSeats(Specification<Seat> spec, Pageable pageable) {
        Page<Seat> pageSeat = seatRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());
        mt.setPages(pageSeat.getTotalPages());
        mt.setTotal(pageSeat.getTotalElements());
        rs.setMeta(mt);
        rs.setResult(pageSeat.getContent());
        return rs;
    }

    @Transactional
    public Seat handleUpdateSeat(Long id, Seat seat) throws IdInvalidException {
        Seat existingSeat = fetchSeatById(id);
        if (existingSeat == null) {
            throw new IdInvalidException("Seat with ID " + id + " does not exist");
        }

        // Validate carriage if provided
        if (seat.getCarriage() != null && seat.getCarriage().getCarriageId() > 0) {
            Long newCarriageId = seat.getCarriage().getCarriageId();
            if (!carriageRepository.existsById(newCarriageId)) {
                throw new IdInvalidException("Carriage with ID " + newCarriageId + " does not exist");
            }
            existingSeat.setCarriage(seat.getCarriage());
        }

        // Update fields
        if (seat.getSeatStatus() != null) {
            existingSeat.setSeatStatus(seat.getSeatStatus());
        }
        if (seat.getSeatType() != null) {
            existingSeat.setSeatType(seat.getSeatType());
        }
        if (seat.getPrice() > 0) {
            existingSeat.setPrice(seat.getPrice());
        }
        // Note: Ticket relationship is managed separately (e.g., via TicketService)

        return seatRepository.save(existingSeat);
    }

    @Transactional
    public void handleDeleteSeat(Long id) {
        seatRepository.deleteById(id);
    }
}