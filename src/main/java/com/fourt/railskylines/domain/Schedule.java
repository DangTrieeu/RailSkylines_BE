package com.fourt.railskylines.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
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
@Table(name = "schedules")
@Getter
@Setter
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
    @JsonIgnore
    private TrainTrip trainTrip;

}
