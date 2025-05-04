package com.fourt.railskylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @ManyToOne
    @JoinColumn(name = "origin_station_id", nullable = false)
    private Station originStation;

    @ManyToMany
    @JoinTable(name = "route_station", joinColumns = @JoinColumn(name = "route_id"), inverseJoinColumns = @JoinColumn(name = "station_id"))
    @JsonIgnoreProperties(value = { "routes" })
    private List<Station> journey;

    @OneToMany(mappedBy = "route")
    @JsonIgnore
    private List<TrainTrip> trainTrips;
}
