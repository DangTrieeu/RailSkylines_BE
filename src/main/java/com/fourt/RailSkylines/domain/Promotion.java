package com.fourt.RailSkylines.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long promotionId;

    private double discount;
    private Date validity;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "promotions")
    @JsonIgnore
    private List<Ticket> tickets;
}
