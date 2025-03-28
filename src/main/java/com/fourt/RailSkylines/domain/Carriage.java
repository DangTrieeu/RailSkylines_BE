package com.fourt.RailSkylines.domain;

import java.util.List;

import com.fourt.RailSkylines.util.constant.CarriageTypeEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "carriages")
public class Carriage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long carriageId;

    // private String type;
    @Enumerated(EnumType.STRING)
    private CarriageTypeEnum carriageType;

    @OneToMany(mappedBy = "carriage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats;

    @ManyToOne()
    @JoinColumn(name = "train_id")
    private Train train;

    public long getCarriageId() {
        return carriageId;
    }

    public void setCarriageId(long carriageId) {
        this.carriageId = carriageId;
    }

    public CarriageTypeEnum getCarriageType() {
        return carriageType;
    }

    public void setCarriageType(CarriageTypeEnum carriageType) {
        this.carriageType = carriageType;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

}
