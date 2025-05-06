package com.fourt.railskylines.controller;

import com.fourt.railskylines.domain.RestResponse;
import com.fourt.railskylines.domain.response.PaymentDTO;
import com.fourt.railskylines.domain.response.PaymentResponse;
import com.fourt.railskylines.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // @GetMapping("/vn-pay")
    // public ResponseObject<PaymentDTO.VNPayResponse> pay(HttpServletRequest
    // request) {
    // return new ResponseObject<>(HttpStatus.OK, "Success",
    // paymentService.createVnPayPayment(request));
    // }
    // @GetMapping("/vn-pay-callback")
    // public ResponseObject<PaymentDTO.VNPayResponse>
    // payCallbackHandler(HttpServletRequest request) {
    // String status = request.getParameter("vnp_ResponseCode");
    // if (status.equals("00")) {
    // return new ResponseObject<>(HttpStatus.OK, "Success", new
    // PaymentDTO.VNPayResponse("00", "Success", ""));
    // } else {
    // return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Failed", null);
    // }
    // }
    @GetMapping("/vn-pay")
    public RestResponse<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        RestResponse<PaymentDTO.VNPayResponse> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setError(null);
        response.setMessage("Success");
        response.setData(paymentService.createVnPayPayment(request));
        return response;
    }

    @GetMapping("/callback")
    public RestResponse<PaymentResponse> payCallbackHandler(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        PaymentResponse paymentResponse = new PaymentResponse();
        RestResponse<PaymentResponse> response = new RestResponse<>();

        if ("00".equals(status)) {
            paymentResponse.setSuccess(true);
            paymentResponse.setTransactionId(request.getParameter("vnp_TransactionNo"));
            paymentResponse.setTxnRef(request.getParameter("vnp_TxnRef"));
            paymentResponse.setMessage("Payment successful. Transaction ID: " +
                    request.getParameter("vnp_TransactionNo") +
                    ", Booking ID: " + request.getParameter("vnp_TxnRef"));

            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Success");
            response.setError(null);
            response.setData(paymentResponse);
        } else {
            paymentResponse.setSuccess(false);
            paymentResponse.setTransactionId(null);
            paymentResponse.setTxnRef(null);
            paymentResponse.setMessage("Payment failed. Response Code: " + status);

            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Failed");
            response.setError("Payment failed");
            response.setData(paymentResponse);
        }

        return response;
    }
}
