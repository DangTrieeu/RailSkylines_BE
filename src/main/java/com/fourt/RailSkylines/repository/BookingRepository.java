package com.fourt.RailSkylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fourt.RailSkylines.domain.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
