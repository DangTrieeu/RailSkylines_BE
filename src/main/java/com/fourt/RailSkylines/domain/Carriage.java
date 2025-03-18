package com.fourt.RailSkylines.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "carriages")
public class Carriage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long carriageId;

}
