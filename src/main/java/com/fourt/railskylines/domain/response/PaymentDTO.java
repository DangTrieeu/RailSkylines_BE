package com.fourt.railskylines.domain.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public abstract class PaymentDTO {
    @Builder
    @Getter
    @Setter
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
    }
}
