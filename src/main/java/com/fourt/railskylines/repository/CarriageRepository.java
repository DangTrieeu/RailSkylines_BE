package com.fourt.railskylines.repository;

import com.fourt.railskylines.domain.Carriage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CarriageRepository extends JpaRepository<Carriage, Long>, JpaSpecificationExecutor<Carriage> {
}