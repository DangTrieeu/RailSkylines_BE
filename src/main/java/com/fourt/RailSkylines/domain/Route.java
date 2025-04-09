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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "routes")
@Getter
@Setter
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

}
