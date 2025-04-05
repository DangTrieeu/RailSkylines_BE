package com.fourt.RailSkylines.domain;

import java.time.Instant;

import com.fourt.RailSkylines.util.constant.CustomerObjectEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ticketId;

    // private enum customerObjectEnum {}
    @Enumerated(EnumType.STRING)
    private CustomerObjectEnum customerObject;

    private Instant startDay;
    private String qrCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @OneToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    public long getTicketId() {
        return ticketId;
    }

    public void setTicketId(long ticketId) {
        this.ticketId = ticketId;
    }

    public CustomerObjectEnum getCustomerObject() {
        return customerObject;
    }

    public void setCustomerObject(CustomerObjectEnum customerObject) {
        this.customerObject = customerObject;
    }

    public Instant getStartDay() {
        return startDay;
    }

    public void setStartDay(Instant startDay) {
        this.startDay = startDay;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

}
