package com.fourt.RailSkylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "trains")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long trainId;

    private String route;
    private String schedule;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Carriage> carriages;

    @ManyToMany
    @JsonIgnore
    private List<Station> stations;
}
