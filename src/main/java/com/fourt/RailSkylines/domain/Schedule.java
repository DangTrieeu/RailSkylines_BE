package com.fourt.RailSkylines.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long scheduleId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "departure_id")
    private ClockTime departure;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "arrival_id")
    private ClockTime arrival;

    @OneToOne(mappedBy = "schedule")
    private TrainTrip trainTrip;

}
