package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Promotion;
import com.fourt.railskylines.domain.request.ReqPromotionDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.service.PromotionService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("/promotions")
    @APIMessage("Fetch All Promotions")
    public ResponseEntity<ResultPaginationDTO> getAllPromotions(
            @Filter Specification<Promotion> specification,
            Pageable pageable) {
        ResultPaginationDTO result = promotionService.getAllPromotionsPaginated(specification, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/promotions/{id}")
    @APIMessage("Fetch Promotion By ID")
    public ResponseEntity<ReqPromotionDTO> getPromotionById(@PathVariable("id") Long id) throws IdInvalidException{
        if (this.promotionService.getPromotionById(id) == null) {
            throw new IdInvalidException("Promotion with id = not exits " + id + " , pls check again");
        }       
        ReqPromotionDTO promotion = promotionService.getPromotionById(id);
        return new ResponseEntity<>(promotion, HttpStatus.OK);
    }

    @PostMapping("/promotions")
    @APIMessage("Create New Promotion")
    public ResponseEntity<ReqPromotionDTO> createPromotion(@RequestBody ReqPromotionDTO promotionDTO) {
        ReqPromotionDTO createdPromotion = promotionService.createPromotion(promotionDTO);
        return new ResponseEntity<>(createdPromotion, HttpStatus.CREATED);
    }

    @PutMapping("/promotions/{id}")
    @APIMessage("Update Promotion")
    public ResponseEntity<ReqPromotionDTO> updatePromotion(@PathVariable("id") Long id, @RequestBody ReqPromotionDTO promotionDTO)throws IdInvalidException {       
        ReqPromotionDTO updatedPromotion = promotionService.updatePromotion(id, promotionDTO);
        return new ResponseEntity<>(updatedPromotion, HttpStatus.OK);
    }

    @DeleteMapping("/promotions/{id}")
    @APIMessage("Delete Promotion")
    public ResponseEntity<Void> deletePromotion(@PathVariable("id") Long id) 
    throws IdInvalidException{     
        promotionService.deletePromotion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}