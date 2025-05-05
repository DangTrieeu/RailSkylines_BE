package com.fourt.railskylines.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fourt.railskylines.domain.Promotion;
import com.fourt.railskylines.util.constant.PromotionStatusEnum;

public interface PromotionRepository extends JpaRepository<Promotion, Long>,JpaSpecificationExecutor<Promotion> {
    // List<Promotion> findByIdIn(List<Long> promotionIds);

    @Query("SELECT p FROM Promotion p WHERE p.status = :status AND p.startDate <= :startDate AND p.validity >= :validity")
    List<Promotion> findByStatusAndStartDateBeforeAndValidityAfter(
            @Param("status") PromotionStatusEnum status,
            @Param("startDate") Instant startDate,
            @Param("validity") Instant validity);

    @Query("SELECT p FROM Promotion p WHERE p.status = :status AND p.validity < :validity")
    List<Promotion> findByStatusAndValidityBefore(
            @Param("status") PromotionStatusEnum status,
            @Param("validity") Instant validity);
}