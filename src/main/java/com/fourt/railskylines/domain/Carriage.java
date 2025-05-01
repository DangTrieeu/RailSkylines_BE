package com.fourt.railskylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourt.railskylines.util.constant.CarriageTypeEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "carriages")
@Getter
@Setter
public class Carriage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long carriageId;

    // private String type;
    @Enumerated(EnumType.STRING)
    private CarriageTypeEnum carriageType;

    private double price;

    private double discount;

    @OneToMany(mappedBy = "carriage", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Seat> seats;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

}
