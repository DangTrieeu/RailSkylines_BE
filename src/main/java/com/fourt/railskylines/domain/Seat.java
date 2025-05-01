package com.fourt.railskylines.domain;

import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.SeatTypeEnum;

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

    @Enumerated(EnumType.STRING)
    private SeatTypeEnum seatType;

}
