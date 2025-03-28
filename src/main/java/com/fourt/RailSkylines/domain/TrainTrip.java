package com.fourt.RailSkylines.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "train_trips")
public class TrainTrip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long trainTripId;

    @OneToOne(mappedBy = "trip")
    private Train train;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @OneToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;
}
