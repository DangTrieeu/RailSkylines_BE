package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Promotion;
import com.fourt.railskylines.domain.request.ReqPromotionDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.repository.PromotionRepository;
import com.fourt.railskylines.util.constant.PromotionStatusEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;

    @Autowired
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public ResultPaginationDTO getAllPromotionsPaginated(Specification<Promotion> specification, Pageable pageable) {
        Page<Promotion> page = promotionRepository.findAll(specification, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1); // Pageable bắt đầu từ 0, tăng lên 1 để dễ hiểu
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::mapToDTO)
                .toList());

        return result;
    }

    public ReqPromotionDTO createPromotion(ReqPromotionDTO promotionDTO) {
        if (promotionDTO.getValidity() == null) {
            throw new IllegalArgumentException("Validity cannot be null");
        }
        if (promotionDTO.getStartDate() != null && promotionDTO.getStartDate().isAfter(promotionDTO.getValidity())) {
            throw new IllegalArgumentException("Start date cannot be after validity date");
        }

        Promotion promotion = new Promotion();
        promotion.setPromotionCode(promotionDTO.getPromotionCode());
        promotion.setPromotionDescription(promotionDTO.getPromotionDescription());
        promotion.setPromotionName(promotionDTO.getPromotionName());
        promotion.setDiscount(promotionDTO.getDiscount());
        promotion.setStartDate(promotionDTO.getStartDate() != null ? promotionDTO.getStartDate() : Instant.now());
        promotion.setValidity(promotionDTO.getValidity());
        Instant now = Instant.now();
        promotion.setStatus(now.isAfter(promotion.getStartDate()) && now.isBefore(promotion.getValidity())
                ? PromotionStatusEnum.active
                : PromotionStatusEnum.inactive);
        promotion = promotionRepository.save(promotion);
        return mapToDTO(promotion);
    }

    public Page<Promotion> getAllPromotions(Specification<Promotion> specification, Pageable pageable) {
        return promotionRepository.findAll(specification, pageable);
    }

    public List<ReqPromotionDTO> getAllActivePromotions() {
        Instant now = Instant.now();
        return promotionRepository.findByStatusAndStartDateBeforeAndValidityAfter(
                PromotionStatusEnum.active, now, now)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ReqPromotionDTO> getAllExpiredPromotions() {
        Instant now = Instant.now();
        return promotionRepository.findByStatusAndValidityBefore(
                PromotionStatusEnum.expired, now)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReqPromotionDTO getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        updatePromotionStatus(promotion);
        return mapToDTO(promotion);
    }

    public ReqPromotionDTO updatePromotion(Long id, ReqPromotionDTO promotionDTO) {
        if (promotionDTO.getValidity() == null) {
            throw new IllegalArgumentException("Validity cannot be null");
        }
        if (promotionDTO.getStartDate() != null && promotionDTO.getStartDate().isAfter(promotionDTO.getValidity())) {
            throw new IllegalArgumentException("Start date cannot be after validity date");
        }

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        promotion.setPromotionCode(promotionDTO.getPromotionCode());
        promotion.setPromotionDescription(promotionDTO.getPromotionDescription());
        promotion.setPromotionName(promotionDTO.getPromotionName());
        promotion.setDiscount(promotionDTO.getDiscount());
        promotion.setStartDate(
                promotionDTO.getStartDate() != null ? promotionDTO.getStartDate() : promotion.getStartDate());
        promotion.setValidity(promotionDTO.getValidity());
        if (promotionDTO.getStatus() != null) {
            promotion.setStatus(promotionDTO.getStatus());
        } else {
            Instant now = Instant.now();
            promotion.setStatus(now.isAfter(promotion.getStartDate()) && now.isBefore(promotion.getValidity())
                    ? PromotionStatusEnum.active
                    : now.isAfter(promotion.getValidity()) ? PromotionStatusEnum.expired
                            : PromotionStatusEnum.inactive);
        }
        promotion = promotionRepository.save(promotion);
        return mapToDTO(promotion);
    }

    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found");
        }
        promotionRepository.deleteById(id);
    }

    public ReqPromotionDTO updatePromotionStatusManually(Long id, PromotionStatusEnum status) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        promotion.setStatus(status);
        promotion = promotionRepository.save(promotion);
        return mapToDTO(promotion);
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void updateAllPromotionStatuses() {
        Instant now = Instant.now();
        List<Promotion> promotions = promotionRepository.findAll();
        for (Promotion promotion : promotions) {
            PromotionStatusEnum newStatus = now.isAfter(promotion.getValidity()) ? PromotionStatusEnum.expired
                    : now.isAfter(promotion.getStartDate()) && now.isBefore(promotion.getValidity())
                            ? PromotionStatusEnum.active
                            : PromotionStatusEnum.inactive;
            if (promotion.getStatus() != newStatus) {
                promotion.setStatus(newStatus);
                promotionRepository.save(promotion);
            }
        }
    }

    private void updatePromotionStatus(Promotion promotion) {
        Instant now = Instant.now();
        PromotionStatusEnum newStatus = now.isAfter(promotion.getValidity()) ? PromotionStatusEnum.expired
                : now.isAfter(promotion.getStartDate()) && now.isBefore(promotion.getValidity())
                        ? PromotionStatusEnum.active
                        : PromotionStatusEnum.inactive;
        if (promotion.getStatus() != newStatus) {
            promotion.setStatus(newStatus);
            promotionRepository.save(promotion);
        }
    }

    public ReqPromotionDTO mapToDTO(Promotion promotion) {
        ReqPromotionDTO dto = new ReqPromotionDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setPromotionCode(promotion.getPromotionCode());
        dto.setPromotionDescription(promotion.getPromotionDescription());
        dto.setPromotionName(promotion.getPromotionName());
        dto.setDiscount(promotion.getDiscount());
        dto.setStartDate(promotion.getStartDate());
        dto.setValidity(promotion.getValidity());
        dto.setStatus(promotion.getStatus());
        return dto;
    }
}