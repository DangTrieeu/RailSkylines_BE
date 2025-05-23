package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.domain.response.BookingResponseDTO;
import com.fourt.railskylines.domain.response.ResBookingHistoryDTO;
import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.service.BookingService;
import com.fourt.railskylines.util.SecurityUtil;
import com.turkraft.springfilter.boot.Filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

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

    // @PostMapping("/bookings")
    // @Transactional
    // public ResponseEntity<RestResponse<String>> createBooking(
    // @RequestParam("tickets") String ticketsParam,
    // @RequestParam(value = "trainTripId") Long trainTripId,
    // @RequestBody @Valid BookingRequestDTO request,
    // HttpServletRequest httpServletRequest) throws Exception {
    // if (trainTripId == null) {
    // RestResponse<String> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.BAD_REQUEST.value());
    // response.setMessage("trainTripId is required in query parameter");
    // response.setData(null);
    // response.setError("Invalid request");
    // return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    // }
    // request.setTrainTripId(trainTripId);

    // List<Map<String, Object>> tickets = objectMapper.readValue(ticketsParam,
    // List.class);
    // List<Long> seatIds = new ArrayList<>();
    // for (Map<String, Object> ticket : tickets) {
    // Long seatNumber = ((Number) ticket.get("seatNumber")).longValue();
    // seatIds.add(seatNumber);
    // Object boardingStationIdObj = ticket.get("boardingStationId");
    // Object alightingStationIdObj = ticket.get("alightingStationId");
    // if (boardingStationIdObj == null || alightingStationIdObj == null) {
    // throw new IllegalArgumentException(
    // "boardingStationId and alightingStationId must be provided in tickets
    // param");
    // }
    // }

    // if (seatIds == null || seatIds.isEmpty()) {
    // RestResponse<String> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.BAD_REQUEST.value());
    // response.setMessage("Seat IDs must not be empty");
    // response.setData(null);
    // response.setError("Invalid request");
    // return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    // }

    // request.setSeatIds(seatIds);
    // request.setTicketsParam(ticketsParam);

    // if (seatIds.size() != request.getTickets().size()) {
    // RestResponse<String> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.BAD_REQUEST.value());
    // response.setMessage("Số lượng ghế (" + seatIds.size() + ") không khớp với số
    // lượng vé ("
    // + request.getTickets().size() + ")");
    // response.setData(null);
    // response.setError("Invalid request");
    // return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    // }

    // // Kiểm tra contactEmail cho người không đăng ký
    // String email = SecurityUtil.getCurrentUserLogin().orElse(null);
    // if (email == null && (request.getContactEmail() == null ||
    // request.getContactEmail().isBlank())) {
    // RestResponse<String> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.BAD_REQUEST.value());
    // response.setMessage("Contact email is required for non-registered users");
    // response.setData(null);
    // response.setError("Invalid request");
    // return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    // }

    // Booking booking = bookingService.createBooking(request, httpServletRequest);
    // String paymentUrl = bookingService.getPaymentUrl(booking,
    // httpServletRequest);

    // RestResponse<String> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.OK.value());
    // response.setMessage("Booking created successfully");
    // response.setData(paymentUrl);
    // response.setError(null);

    // return ResponseEntity.ok(response);
    // }
    @PostMapping("/bookings")
    @Transactional
    public ResponseEntity<RestResponse<String>> createBooking(
            @RequestParam("tickets") String ticketsParam,
            @RequestParam(value = "trainTripId") Long trainTripId,
            @RequestBody @Valid BookingRequestDTO request,
            HttpServletRequest httpServletRequest) throws Exception {
        if (trainTripId == null) {
            RestResponse<String> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("trainTripId is required in query parameter");
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        request.setTrainTripId(trainTripId);
        request.setTicketsParam(ticketsParam);

        List<Map<String, Object>> tickets = objectMapper.readValue(ticketsParam, List.class);
        List<Long> seatIds = new ArrayList<>();
        for (Map<String, Object> ticket : tickets) {
            Long seatNumber = ((Number) ticket.get("seatNumber")).longValue();
            seatIds.add(seatNumber);
            if (ticket.get("boardingStationId") == null || ticket.get("alightingStationId") == null) {
                throw new IllegalArgumentException(
                        "boardingStationId and alightingStationId must be provided in tickets param");
            }
        }
        request.setSeatIds(seatIds);

        Booking booking = bookingService.createBooking(request, httpServletRequest);
        String paymentUrl = bookingService.getPaymentUrl(booking, httpServletRequest);

        RestResponse<String> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage("Booking created successfully");
        response.setData(paymentUrl);
        response.setError(null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookings/history")
    public ResponseEntity<RestResponse<List<ResBookingHistoryDTO>>> getBookingHistory() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));

        try {
            List<ResBookingHistoryDTO> bookings = bookingService.getBookingHistoryByUser(email);
            RestResponse<List<ResBookingHistoryDTO>> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Booking history retrieved successfully");
            response.setData(bookings);
            response.setError(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<List<ResBookingHistoryDTO>> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/bookings/search")
    public ResponseEntity<RestResponse<Booking>> searchBooking(
            @RequestParam("bookingCode") String bookingCode,
            @RequestParam("vnpTxnRef") String vnpTxnRef) {
        try {
            Booking booking = bookingService.findBookingByCodeAndVnpTxnRef(bookingCode, vnpTxnRef);
            RestResponse<Booking> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Booking retrieved successfully");
            response.setData(booking);
            response.setError(null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RestResponse<Booking> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setError("Invalid request");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<ResultPaginationDTO> getAllBookings(@Filter Specification<Booking> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(
                this.bookingService.getAllBookings(spec, pageable));
    }

    // @GetMapping("/bookings/{id}")
    // public ResponseEntity<RestResponse<Booking>>
    // getBookingById(@PathVariable("id") Long id) {
    // try {
    // Booking booking = bookingService.getBookingById(id);
    // RestResponse<Booking> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.OK.value());
    // response.setMessage("Booking retrieved successfully");
    // response.setData(booking);
    // response.setError(null);
    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // RestResponse<Booking> response = new RestResponse<>();
    // response.setStatusCode(HttpStatus.NOT_FOUND.value());
    // response.setMessage(e.getMessage());
    // response.setData(null);
    // response.setError("Booking not found");
    // return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    // }
    // }
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<RestResponse<BookingResponseDTO>> getBookingById(
            @PathVariable("bookingId") String bookingId) {
        try {
            BookingResponseDTO booking = bookingService.getBookingById(bookingId);
            RestResponse<BookingResponseDTO> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Booking retrieved successfully");
            response.setData(booking);
            response.setError(null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            RestResponse<BookingResponseDTO> response = new RestResponse<>();
            response.setStatusCode(HttpStatus.NOT_FOUND.value());
            response.setMessage(e.getMessage());
            response.setData(null);
            response.setError("Booking not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}