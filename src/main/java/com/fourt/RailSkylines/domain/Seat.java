package com.fourt.RailSkylines.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long seatId;

    private boolean status;
    private double price;

    @ManyToOne()
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne()
    @JoinColumn(name = "carriage_id")
    private Carriage carriage;
}
