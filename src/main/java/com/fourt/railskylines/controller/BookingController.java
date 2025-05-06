package com.fourt.railskylines.controller;

import com.fourt.railskylines.config.VNPayConfig;
import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.request.BookingRequestDTO;
import com.fourt.railskylines.domain.response.PaymentResponse;
import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.integration.VNPayPaymentGateway;
import com.fourt.railskylines.service.BookingService;
import com.fourt.railskylines.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;
    private final VNPayConfig vnpayConfig;
    private final VNPayPaymentGateway paymentGateway;

    public BookingController(BookingService bookingService, VNPayConfig vnpayConfig,
            VNPayPaymentGateway paymentGateway) {
        this.bookingService = bookingService;
        this.vnpayConfig = vnpayConfig;
        this.paymentGateway = paymentGateway;
    }

    @PostMapping("/bookings")
    @Transactional
    public ResponseEntity<RestResponse<String>> createBooking(
            @RequestBody @Valid BookingRequestDTO request,
            HttpServletRequest httpRequest) {
        RestResponse<String> response = new RestResponse<>();

        // Validate contact information for non-registered users
        if (request.getUserId() == null && (request.getContactEmail() == null || request.getContactEmail().isBlank())) {
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setError("Validation Error");
            response.setMessage("Contact email is required for non-registered users");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Create the booking
            Booking booking = bookingService.createBooking(request);
            if (booking.getBookingId() <= 0) {
                throw new IllegalStateException("Failed to create booking");
            }

            // Generate payment URL
            String txnRef = String.valueOf(System.currentTimeMillis());
            PaymentResponse paymentResponse = paymentGateway.processPayment(
                    booking.getTotalPrice(),
                    String.valueOf(booking.getBookingId()),
                    httpRequest);

            if (!paymentResponse.isSuccess()) {
                throw new RuntimeException(paymentResponse.getMessage());
            }

            // Update booking with transaction ID and VNPay transaction reference
            booking.setTransactionId(paymentResponse.getTransactionId());
            booking.setVnpTxnRef(txnRef);
            bookingService.updateBooking(booking);

            response.setStatusCode(HttpStatus.OK.value());
            response.setData(paymentResponse.getPaymentUrl());
            response.setMessage("Payment URL generated successfully. Booking Code: " + booking.getBookingCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to generate payment URL for booking: {}", e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("Payment Error");
            response.setMessage("Failed to generate payment URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/bookings/history")
    public ResponseEntity<RestResponse<List<Booking>>> getBookingHistory() {
        RestResponse<List<Booking>> response = new RestResponse<>();
        String username = null;
        try {
            username = SecurityUtil.getCurrentUserLogin()
                    .orElseThrow(() -> new RuntimeException("User not authenticated"));
            List<Booking> bookings = bookingService.getBookingsByUser(username);
            response.setStatusCode(HttpStatus.OK.value());
            response.setData(bookings);
            response.setMessage("Booking history retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to retrieve booking history for user {}: {}", username, e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("Server Error");
            response.setMessage("Failed to retrieve booking history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/payments/return")
    public ResponseEntity<PaymentResponse> handleVNPayReturn(@RequestParam Map<String, String> params, HttpServletRequest request) {
        PaymentResponse response = new PaymentResponse();

        try {
            // Verify the signature
            boolean isValid = vnpayConfig.verifyReturn(params);
            if (!isValid) {
                response.setSuccess(false);
                response.setTransactionId(params.get("vnp_TxnRef"));
                response.setMessage("Chữ ký không hợp lệ. Xác minh thanh toán thất bại.");
                return ResponseEntity.badRequest().body(response);
            }

            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            response.setTransactionId(txnRef);

            if (txnRef == null || responseCode == null) {
                response.setSuccess(false);
                response.setMessage("Thiếu tham số bắt buộc: vnp_TxnRef hoặc vnp_ResponseCode.");
                return ResponseEntity.badRequest().body(response);
            }

            Booking booking = bookingService.findBookingByTxnRef(txnRef);
            if (booking == null) {
                response.setSuccess(false);
                response.setMessage("Không tìm thấy đặt chỗ cho mã giao dịch: " + txnRef);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Construct payment URL with parameters matching the initial payment request
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpayConfig.getVnpTmnCode());
            vnpParams.put("vnp_Amount", params.get("vnp_Amount")); // Amount from return params
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", params.get("vnp_OrderInfo")); // OrderInfo from return params
            vnpParams.put("vnp_OrderType", vnpayConfig.getVnpOrderType());
            vnpParams.put("vnp_Locale", vnpayConfig.getVnpUrl());
            vnpParams.put("vnp_ReturnUrl", vnpayConfig.getVnpReturnUrl());
            vnpParams.put("vnp_IpAddr", vnpayConfig.getIpAddress(request));

            // Add dates
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(new Date());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            cld.add(Calendar.MINUTE, 15); // 15-minute expiration
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Generate secure hash
            StringBuilder hashData = new StringBuilder();
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    hashData.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            String vnpSecureHash = vnpayConfig.hmacSHA512(vnpayConfig.getVnpHashSecret(), hashData.substring(0, hashData.length() - 1));
            vnpParams.put("vnp_SecureHash", vnpSecureHash);

            // Build payment URL
            StringBuilder query = new StringBuilder();
            for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                         .append("=")
                         .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                         .append("&");
                }
            }
            String paymentUrl = vnpayConfig.getVnpUrl() + "?" + query.substring(0, query.length() - 1);
            response.setPaymentUrl(paymentUrl);

            // Process payment status
            if ("00".equals(responseCode)) {
                String transactionNo = params.get("vnp_TransactionNo");
                bookingService.updateBookingPaymentStatus(txnRef, true, transactionNo);

                response.setSuccess(true);
                response.setTransactionId(transactionNo);
                response.setMessage("Thanh toán thành công. Mã giao dịch VNPAY: " + transactionNo + ". Mã đặt chỗ: " + booking.getBookingCode());
                return ResponseEntity.ok(response);
            } else {
                bookingService.updateBookingPaymentStatus(txnRef, false, null);

                response.setSuccess(false);
                response.setMessage("Thanh toán thất bại. Mã lỗi VNPAY: " + responseCode + ". Mã giao dịch: " + txnRef);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Lỗi xử lý callback VNPay cho TxnRef [{}]: {}", params.get("vnp_TxnRef"), e.getMessage(), e);
            response.setSuccess(false);
            response.setTransactionId(params.get("vnp_TxnRef"));
            response.setMessage("Lỗi máy chủ khi xử lý thanh toán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String buildVnPayGatewayUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }
        Map<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder queryParams = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                try {
                    String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                    String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                    if (queryParams.length() > 0) {
                        queryParams.append("&");
                    }
                    queryParams.append(encodedKey).append("=").append(encodedValue);
                } catch (Exception e) {
                    logger.error("Lỗi mã hóa tham số URL: {} = {}", entry.getKey(), entry.getValue(), e);
                }
            }
        }
        return queryParams.length() == 0 ? baseUrl : baseUrl + "?" + queryParams;
    }
}