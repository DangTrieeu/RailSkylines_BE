package com.fourt.RailSkylines.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.RailSkylines.domain.Booking;
import com.fourt.RailSkylines.domain.Seat;
import com.fourt.RailSkylines.domain.Ticket;
import com.fourt.RailSkylines.domain.User;
import com.fourt.RailSkylines.service.BookingService;
import com.fourt.RailSkylines.util.constant.CustomerObjectEnum;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/seats")
    public ResponseEntity<List<Seat>> getSeats(@RequestParam Long trainTripId) {
        return ResponseEntity.ok(bookingService.getAvailableSeats(trainTripId));
    }

    @PostMapping("/add-seat")
    public ResponseEntity<Ticket> addSeat(@RequestParam Long seatId,
            @RequestParam CustomerObjectEnum customerType,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.addSeatToCart(user, seatId, customerType));
    }

    @PostMapping("/create")
    public ResponseEntity<Booking> createBooking(@AuthenticationPrincipal User user,
            @RequestBody List<Long> ticketIds) {
        return ResponseEntity.ok(bookingService.createBooking(user, ticketIds));
    }

    @PostMapping("/{bookingId}/pay")
    public ResponseEntity<Booking> pay(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.completePayment(bookingId));
    }
}
