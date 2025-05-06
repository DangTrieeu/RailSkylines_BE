package com.fourt.railskylines.domain;

import java.time.Instant;
import java.util.List;

import com.fourt.railskylines.util.constant.PromotionStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private String promotionCode;
    private String promotionDescription;
    private String promotionName;

    @Column(nullable = false)
    private double discount;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant validity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PromotionStatusEnum status;

    @ManyToMany
    @JoinTable(name = "booking_promotion", joinColumns = @JoinColumn(name = "promotion_id"), inverseJoinColumns = @JoinColumn(name = "booking_id"))
    private List<Booking> bookings;
}