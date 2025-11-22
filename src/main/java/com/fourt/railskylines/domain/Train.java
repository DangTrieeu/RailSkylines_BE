package com.fourt.railskylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fourt.railskylines.util.constant.TrainStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trains")
@Getter
@Setter
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long trainId;

    private String trainName;

    // private enum trainStatusEnum {}
    @Enumerated(EnumType.STRING)
    private TrainStatusEnum trainStatus;

    // @OneToOne
    // @JoinColumn(name = "train_trip_id")
    // private TrainTrip trip;
    @OneToMany(mappedBy = "train", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TrainTrip> trip;

    @OneToMany(mappedBy = "train", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Carriage> carriages;

}
