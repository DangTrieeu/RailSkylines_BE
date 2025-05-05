package com.fourt.railskylines.controller;

import com.fourt.railskylines.config.VNPayConfig;
import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.service.BookingService;
import com.fourt.railskylines.util.SecurityUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingService bookingService;
    private final VNPayConfig vnpayConfig;

    public BookingController(BookingService bookingService, VNPayConfig vnpayConfig) {
        this.bookingService = bookingService;
        this.vnpayConfig = vnpayConfig;
    }

    @PostMapping("/bookings")
    @Transactional
    public ResponseEntity<String> createBooking(@RequestBody @Valid BookingRequestDTO request) {
        if (request.getUserId() == null && (request.getContactEmail() == null ||
                request.getContactEmail().isBlank())) {
            throw new IllegalArgumentException("Contact email is required for non-registered users");
        }
        Booking booking = bookingService.createBooking(request);
        String paymentUrl = bookingService.getPaymentUrl(booking.getTotalPrice());
        return ResponseEntity.ok(paymentUrl); // Trả về URL để redirect người dùng
    }

    @GetMapping("/bookings/history")
    public ResponseEntity<List<Booking>> getBookingHistory() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        List<Booking> bookings = bookingService.getBookingsByUser(username);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/payments/return")
    public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> params) throws Exception {
        boolean isValid = vnpayConfig.verifyReturn(params);
        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            String transactionNo = params.get("vnp_TransactionNo");
            bookingService.updateBookingPaymentStatus(params.get("vnp_TxnRef"), true,
                    transactionNo);
            return ResponseEntity.ok("Payment successful. Transaction ID: " +
                    transactionNo);
        } else {
            bookingService.updateBookingPaymentStatus(params.get("vnp_TxnRef"), false,
                    null);
            return ResponseEntity.badRequest().body("Payment failed. Response Code: " +
                    responseCode);
        }
    }
}