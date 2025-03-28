package com.fourt.RailSkylines.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clock_times")
public class ClockTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long clockTimeId;

    private Date date;
    private double hour;
    private double minute;

}
