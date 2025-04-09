package com.fourt.RailSkylines.domain;

import com.fourt.RailSkylines.util.constant.SeatStatusEnum;

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
@Table(name = "seats")
@Getter
@Setter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long seatId;

    // private boolean status;
    @Enumerated(EnumType.STRING)
    private SeatStatusEnum seatStatus;

    private double price;

    @OneToOne(mappedBy = "seat")
    private Ticket ticket;

    @ManyToOne()
    @JoinColumn(name = "carriage_id")
    private Carriage carriage;

}
