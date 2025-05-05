package com.fourt.railskylines.integration;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fourt.railskylines.config.VNPayConfig;
import com.fourt.railskylines.domain.response.PaymentResponse;


@Component
public class VNPayPaymentGateway implements PaymentGateway {
    private static final Logger logger = LoggerFactory.getLogger(VNPayPaymentGateway.class);
    private final VNPayConfig vnpayConfig;
    private final OkHttpClient okHttpClient;

    public VNPayPaymentGateway(VNPayConfig vnpayConfig, OkHttpClient okHttpClient) {
        this.vnpayConfig = vnpayConfig;
        this.okHttpClient = okHttpClient;
    }

    @Override
    public PaymentResponse processPayment(double amount) {
        try {
            String orderInfo = "Thanh toán đặt vé tàu - " + System.currentTimeMillis();
            String ipAddr = "127.0.0.1"; // Thay bằng IP thực tế của client
            String paymentUrl = vnpayConfig.createPaymentUrl(amount, orderInfo, ipAddr);

            // Gửi request đến VNPay (trong thực tế, redirect người dùng)
            Request request = new Request.Builder().url(paymentUrl).build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("Payment URL generated: {}", paymentUrl);
                    return new PaymentResponse(true, "txn_" + System.currentTimeMillis(), paymentUrl);
                } else {
                    logger.error("VNPay request failed: {}", response.message());
                    return new PaymentResponse(false, null, "Payment request failed");
                }
            }
        } catch (Exception e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            return new PaymentResponse(false, null, "Payment failed: " + e.getMessage());
        }
    }
}