package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.response.RestResponse;
import com.fourt.railskylines.domain.response.PaymentDTO;
import com.fourt.railskylines.domain.response.PaymentResponse;
import com.fourt.railskylines.service.BookingService;
import com.fourt.railskylines.service.PaymentService;
import com.fourt.railskylines.config.VNPayConfig;
import com.fourt.railskylines.util.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin("https://railskylines-fe-1.onrender.com")
public class PaymentController {
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final VNPayConfig vnPayConfig;

    @GetMapping("/vn-pay")
    public RestResponse<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        String amountStr = request.getParameter("amount");
        if (amountStr == null) {
            throw new IllegalArgumentException("Amount parameter is required");
        }
        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amountStr);
        }
        String bankCode = request.getParameter("bankCode");
        String txnRef = request.getParameter("txnRef"); // Lấy txnRef từ request nếu có

        RestResponse<PaymentDTO.VNPayResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setError(null);
        response.setMessage("Success");
        response.setData(paymentService.createVnPayPayment(request, amount, bankCode,
                txnRef != null ? txnRef : VNPayUtil.getRandomNumber(8)));
        return response;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> payCallbackHandler(HttpServletRequest request) {
        // Validate VNPay secure hash
        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        Map<String, String> vnpParams = new HashMap<>();
        for (String paramName : request.getParameterMap().keySet()) {
            if (!paramName.equals("vnp_SecureHash")) {
                vnpParams.put(paramName, request.getParameter(paramName));
            }
        }
        String hashData = VNPayUtil.getPaymentURL(vnpParams, false);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        String vnpTxnRef = request.getParameter("vnp_TxnRef"); // e.g., C977106C
        String status = request.getParameter("vnp_ResponseCode");
        String transactionNo = request.getParameter("vnp_TransactionNo"); // e.g., 14949057

        if (!calculatedHash.equals(vnpSecureHash)) {
            String redirectUrl = "http://localhost:3000/payment?error=InvalidSecureHash";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        }

        boolean success = "00".equals(status);
        try {
            // Update booking status
            bookingService.updateBookingPaymentStatus(vnpTxnRef, success, transactionNo);

            if (success) {
                // Redirect to frontend with bookingId (vnpTxnRef)
                String redirectUrl = "http://localhost:3000/booking-confirmation?bookingId=" + vnpTxnRef;
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            } else {
                String redirectUrl = "http://localhost:3000/payment?error=PaymentFailed&responseCode=" + status;
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            }
        } catch (RuntimeException e) {
            String redirectUrl = "http://localhost:3000/payment?error=BookingUpdateFailed";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        }
    }
}