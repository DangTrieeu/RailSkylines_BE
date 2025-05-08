package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    public BookingController(BookingService bookingService, ObjectMapper objectMapper) {
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/bookings")
    @Transactional
    public ResponseEntity<RestResponse<String>> createBooking(
            @RequestParam("tickets") String ticketsParam,
            @RequestBody @Valid BookingRequestDTO request,
            HttpServletRequest httpServletRequest) throws Exception {
        // Parse tickets từ URL param
        List<Map<String, Object>> tickets = objectMapper.readValue(ticketsParam, List.class);
        List<Long> seatIds = new ArrayList<>();
        for (Map<String, Object> ticket : tickets) {
            Long seatNumber = ((Number) ticket.get("seatNumber")).longValue();
            seatIds.add(seatNumber);
        }

        // Kiểm tra thủ công seatIds không rỗng
        if (seatIds == null || seatIds.isEmpty()) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Seat IDs must not be empty");
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        request.setSeatIds(seatIds);
        request.setTicketsParam(ticketsParam);

        // Kiểm tra số lượng tickets trong body và seatIds
        if (seatIds.size() != request.getTickets().size()) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Số lượng ghế (" + seatIds.size() + ") không khớp với số lượng vé (" + request.getTickets().size() + ")");
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Validate thông tin liên hệ
        if (request.getUserId() == null && (request.getContactEmail() == null || request.getContactEmail().isBlank())) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Contact email is required for non-registered users");
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Validate promotionId
        if (request.getPromotionId() != null) {
            // Optional: Add additional validation if needed
        }

        // Tạo booking
        Booking booking = bookingService.createBooking(request, httpServletRequest);

        // Tạo URL thanh toán
        String paymentUrl = bookingService.getPaymentUrl(booking, httpServletRequest);

        // Trả về response
        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Booking created successfully");
        response.setData(paymentUrl);
        response.setError(null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/history")
    public ResponseEntity<RestResponse<List<Booking>>> getBookingHistory(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getRemoteUser();
        if (username == null) {
            throw new RuntimeException("User not authenticated");
        }
        List<Booking> bookings = bookingService.getBookingsByUser(username);

        RestResponse<List<Booking>> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Booking history retrieved successfully");
        response.setData(bookings);
        response.setError(null);

        return ResponseEntity.ok(response);
    }
}