package com.fourt.railskylines.domain;

import java.time.Instant;

import com.fourt.railskylines.util.constant.CustomerObjectEnum;

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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ticketId;

    // private enum customerObjectEnum {}
    @Enumerated(EnumType.STRING)
    private CustomerObjectEnum customerObject;

    private Instant startDay;
    private String qrCode;
    private Instant payAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @OneToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

}
