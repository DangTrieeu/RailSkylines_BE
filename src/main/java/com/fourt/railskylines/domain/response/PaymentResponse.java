package com.fourt.railskylines.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse {
    private boolean success;
    private String transactionId;
    private String paymentUrl;
    private String message;

    public PaymentResponse() {
    }

    public PaymentResponse(boolean success, String transactionId, String paymentUrl, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.paymentUrl = paymentUrl;
        this.message = message;
    }
}