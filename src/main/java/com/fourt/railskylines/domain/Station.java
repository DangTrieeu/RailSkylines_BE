package com.fourt.railskylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stations")
@Getter
@Setter
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stationId;
    private String stationName;
    private double position;

    // @ManyToMany(mappedBy = "journey")
    // @JsonIgnoreProperties(value = { "stations" })
    // private List<Route> routes;

    @ManyToMany(mappedBy = "journey")
    @JsonIgnore
    private List<Route> routes;
}
