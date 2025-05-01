package com.fourt.railskylines.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "train_trips")
@Getter
@Setter
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
