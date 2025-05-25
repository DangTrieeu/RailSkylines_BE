package com.fourt.railskylines.domain;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clock_times")
@Getter
@Setter
public class ClockTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long clockTimeId;

    private Instant date;
    private double hour;
    private double minute;

}
