package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.repository.BookingRepository;
import com.fourt.railskylines.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    public TicketService(TicketRepository ticketRepository, BookingRepository bookingRepository) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
    }

    public Ticket findByTicketCodeAndCitizenId(String ticketCode, String citizenId) {
        return ticketRepository.findByTicketCodeAndCitizenId(ticketCode, citizenId)
                .orElseThrow(() -> new RuntimeException("Ticket not found or citizen ID does not match"));
    }

    public Booking findBookingByCodeAndCitizenId(String bookingCode, String citizenId) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        boolean isValid = booking.getTickets().stream()
                .anyMatch(ticket -> ticket.getCitizenId().equals(citizenId));
        if (!isValid) {
            throw new RuntimeException("Citizen ID does not match any ticket in this booking");
        }
        return booking;
    }
}