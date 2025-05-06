package com.fourt.railskylines.integration;

import com.fourt.railskylines.config.VNPayConfig;
import com.fourt.railskylines.domain.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VNPayPaymentGateway implements PaymentGateway {
    private static final Logger logger = LoggerFactory.getLogger(VNPayPaymentGateway.class);
    private final VNPayConfig vnpayConfig;

    public VNPayPaymentGateway(VNPayConfig vnpayConfig) {
        this.vnpayConfig = vnpayConfig;
    }

    @Override
    public PaymentResponse processPayment(double amount, String bookingId, HttpServletRequest request) {
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            if (bookingId == null || bookingId.isBlank()) {
                throw new IllegalArgumentException("Booking ID is required");
            }
            if (request == null) {
                throw new IllegalArgumentException("HttpServletRequest is required for IP address");
            }

            String orderInfo = "Thanh toán đặt vé tàu - Booking ID: " + bookingId;
            String txnRef = String.valueOf(System.currentTimeMillis()); // Should match BookingController logic
            String paymentUrl = vnpayConfig.createPaymentUrl(amount, orderInfo, txnRef, request);
            logger.info("Payment URL generated for booking {}: {}", bookingId, paymentUrl);
            return new PaymentResponse(true, "txn_" + System.currentTimeMillis(), paymentUrl, null);
        } catch (Exception e) {
            logger.error("Payment processing failed for booking {}: {}", bookingId, e.getMessage());
            return new PaymentResponse(false, null, null, "Payment failed: " + e.getMessage());
        }
    }
}