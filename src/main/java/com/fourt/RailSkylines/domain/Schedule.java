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

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public ClockTime getDeparture() {
        return departure;
    }

    public void setDeparture(ClockTime departure) {
        this.departure = departure;
    }

    public ClockTime getArrival() {
        return arrival;
    }

    public void setArrival(ClockTime arrival) {
        this.arrival = arrival;
    }

    public TrainTrip getTrainTrip() {
        return trainTrip;
    }

    public void setTrainTrip(TrainTrip trainTrip) {
        this.trainTrip = trainTrip;
    }

}
