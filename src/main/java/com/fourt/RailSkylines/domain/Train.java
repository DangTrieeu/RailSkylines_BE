package com.fourt.RailSkylines.domain;

import java.util.List;

import com.fourt.RailSkylines.util.constant.TrainStatusEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trains")
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long trainId;

    private String TrainName;

    // private enum trainStatusEnum {}
    @Enumerated(EnumType.STRING)
    private TrainStatusEnum trainStatus;

    @OneToOne
    @JoinColumn(name = "train_trip_id")
    private TrainTrip trip;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Carriage> carriages;

    public long getTrainId() {
        return trainId;
    }

    public void setTrainId(long trainId) {
        this.trainId = trainId;
    }

    public String getTrainName() {
        return TrainName;
    }

    public void setTrainName(String trainName) {
        TrainName = trainName;
    }

    public TrainStatusEnum getTrainStatus() {
        return trainStatus;
    }

    public void setTrainStatus(TrainStatusEnum trainStatus) {
        this.trainStatus = trainStatus;
    }

    public TrainTrip getTrip() {
        return trip;
    }

    public void setTrip(TrainTrip trip) {
        this.trip = trip;
    }

    public List<Carriage> getCarriages() {
        return carriages;
    }

    public void setCarriages(List<Carriage> carriages) {
        this.carriages = carriages;
    }

}
