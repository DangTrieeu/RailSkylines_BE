package com.fourt.RailSkylines.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long routeId;

    @OneToOne
    @JoinColumn(name = "origin_station_id")
    private Station originStation;

    @ManyToMany
    @JoinTable(name = "route_station", joinColumns = @JoinColumn(name = "route_id"), inverseJoinColumns = @JoinColumn(name = "station_id"))
    private List<Station> journey;

    @OneToOne(mappedBy = "route")
    private TrainTrip trainTrip;

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public Station getOriginStation() {
        return originStation;
    }

    public void setOriginStation(Station originStation) {
        this.originStation = originStation;
    }

    public List<Station> getJourney() {
        return journey;
    }

    public void setJourney(List<Station> journey) {
        this.journey = journey;
    }

    public TrainTrip getTrainTrip() {
        return trainTrip;
    }

    public void setTrainTrip(TrainTrip trainTrip) {
        this.trainTrip = trainTrip;
    }

}
