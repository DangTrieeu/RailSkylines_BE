package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.response.ResRevenueDTO;
import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.service.RevenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("/total")
    public ResponseEntity<RestResponse<ResRevenueDTO>> getTotalRevenue(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate) {
        RestResponse<ResRevenueDTO> response = new RestResponse<>();
        try {
            Instant start = startDate != null ? Instant.parse(startDate) : null;
            Instant end = endDate != null ? Instant.parse(endDate) : null;
            ResRevenueDTO revenueData = revenueService.calculateRevenueData(start, end);
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Thành công");
            response.setData(revenueData);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Không thể tính toán doanh thu: " + e.getMessage());
            response.setError(e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}