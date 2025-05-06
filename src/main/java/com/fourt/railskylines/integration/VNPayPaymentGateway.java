package com.fourt.railskylines.integration;

import com.fourt.railskylines.config.VNPayConfig;
import com.fourt.railskylines.domain.response.PaymentResponse;
import com.fourt.railskylines.util.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class VNPayPaymentGateway implements PaymentGateway {
    private static final Logger logger = LoggerFactory.getLogger(VNPayPaymentGateway.class);
    private final VNPayConfig vnpayConfig;

    @Override
    public PaymentResponse processPayment(double amount) {
        try {
            // Chuyển đổi amount sang định dạng VNPay (nhân 100)
            long vnpAmount = (long) (amount * 100);

            // Giả định bankCode và IP (có thể tùy chỉnh sau)
            String bankCode = null; // Không chỉ định ngân hàng cụ thể
            String ipAddr = "127.0.0.1"; // Giả định IP, thay bằng IP thực tế nếu cần

            // Lấy cấu hình VNPay cơ bản
            Map<String, String> vnpParamsMap = vnpayConfig.getVNPayConfig();

            // Thêm các tham số thanh toán
            vnpParamsMap.put("vnp_Amount", String.valueOf(vnpAmount));
            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParamsMap.put("vnp_BankCode", bankCode);
            }
            vnpParamsMap.put("vnp_IpAddr", ipAddr);

            // Tạo URL thanh toán
            String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
            String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;

            logger.info("Payment URL generated: {}", paymentUrl);
            return new PaymentResponse(true, "txn_" + VNPayUtil.getRandomNumber(8), paymentUrl);

        } catch (Exception e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            return new PaymentResponse(false, null, "Payment failed: " + e.getMessage());
        }
    }
}