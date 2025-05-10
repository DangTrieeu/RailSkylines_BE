package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.domain.response.TicketResponseDTO;
import com.fourt.railskylines.service.TicketService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<Ticket> searchTicket(
            @RequestParam String ticketCode,
            @RequestParam String citizenId) {
        Ticket ticket = ticketService.findByTicketCodeAndCitizenId(ticketCode, citizenId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/bookings/search")
    public ResponseEntity<Booking> searchBooking(
            @RequestParam String bookingCode,
            @RequestParam String citizenId) {
        Booking booking = ticketService.findBookingByCodeAndCitizenId(bookingCode, citizenId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/tickets/{ticketCode}")
    public ResponseEntity<RestResponse<TicketResponseDTO>> getTicketByCode(
            @PathVariable("ticketCode") String ticketCode) {
        try {
            TicketResponseDTO ticket = ticketService.getTicketByCode(ticketCode);
            RestResponse<TicketResponseDTO> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Ticket retrieved successfully");
            response.setData(ticket);
            response.setError(null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            RestResponse<TicketResponseDTO> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setError("Ticket not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}