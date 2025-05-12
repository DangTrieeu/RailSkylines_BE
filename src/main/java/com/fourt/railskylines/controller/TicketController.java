package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.domain.response.ResTicketHistoryDTO;
import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.service.TicketService;
import com.fourt.railskylines.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<RestResponse<Ticket>> searchTicket(
            @RequestParam("ticketCode") String ticketCode,
            @RequestParam("citizenId") String citizenId) {
        try {
            Ticket ticket = ticketService.findByTicketCodeAndCitizenId(ticketCode, citizenId);
            RestResponse<Ticket> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Ticket retrieved successfully");
            response.setData(ticket);
            response.setError(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Ticket> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/tickets/history")
    public ResponseEntity<RestResponse<List<ResTicketHistoryDTO>>> getTicketHistory() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        try {
            List<ResTicketHistoryDTO> tickets = ticketService.getTicketHistoryByUser(email);
            RestResponse<List<ResTicketHistoryDTO>> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Ticket history retrieved successfully");
            response.setData(tickets);
            response.setError(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<List<ResTicketHistoryDTO>> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}