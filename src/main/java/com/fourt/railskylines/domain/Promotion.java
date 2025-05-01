package com.fourt.railskylines.domain;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "promotions")
@Getter
@Setter
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long promotionId;
    private String promotionName;
    private double discount;
    private Instant validity;

    @ManyToMany
    @JoinTable(name = "booking_promotion", joinColumns = @JoinColumn(name = "promotion_id"), inverseJoinColumns = @JoinColumn(name = "booking_id"))
    private List<Booking> bookings;

}
