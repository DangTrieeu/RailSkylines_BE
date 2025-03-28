package com.fourt.RailSkylines.domain;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long promotionId;
    private String promotionName;
    private double discount;
    private Date validity;

    @ManyToMany
    @JoinTable(name = "booking_promotion", joinColumns = @JoinColumn(name = "promotion_id"), inverseJoinColumns = @JoinColumn(name = "booking_id"))
    private List<Booking> bookings;
}
