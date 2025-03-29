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

    public long getClockTimeId() {
        return clockTimeId;
    }

    public void setClockTimeId(long clockTimeId) {
        this.clockTimeId = clockTimeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getHour() {
        return hour;
    }

    public void setHour(double hour) {
        this.hour = hour;
    }

    public double getMinute() {
        return minute;
    }

    public void setMinute(double minute) {
        this.minute = minute;
    }

}
