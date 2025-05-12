package com.fourt.railskylines.domain;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourt.railskylines.util.constant.CustomerObjectEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tickets", indexes = @jakarta.persistence.Index(name = "idx_citizen_id", columnList = "citizenId"))
@Getter
@Setter
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_object")
    private CustomerObjectEnum customerObject;

    @Column(name = "ticket_code", unique = true)
    private String ticketCode;

    private String name;

    @Column(name = "citizen_id")
    private String citizenId;

    private double price;

    @Column(name = "start_day")
    private Instant startDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status")
    private TicketStatusEnum ticketStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private Integer boardingOrder; // Thêm để lưu index của boardingStation
    private Integer alightingOrder; // Thêm để lưu index của alightingStation

    @PrePersist
    public void prePersist() {
        if (this.ticketCode == null) {
            this.ticketCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}