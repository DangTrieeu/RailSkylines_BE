package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.ClockTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClockTimeRepository extends JpaRepository<ClockTime, Long>, JpaSpecificationExecutor<ClockTime> {
}