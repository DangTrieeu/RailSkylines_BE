package com.fourt.railskylines.domain;

import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seats")
@Getter
@Setter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status")
    private SeatStatusEnum seatStatus;

    private double price;

    @OneToMany(mappedBy = "seat")
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "carriage_id")
    private Carriage carriage;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatTypeEnum seatType;

    @ManyToOne
    @JoinColumn(name = "train_trip_id")
    private TrainTrip trainTrip;
}