package com.fourt.RailSkylines.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ticketId;

    private String ticketStatus;
    private Date startDay;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @OneToMany(mappedBy = "ticket")
    private List<Seat> seats;

    @ManyToMany
    @JsonIgnore
    private List<Promotion> promotions;
}
