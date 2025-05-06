package com.fourt.railskylines.integration;

import com.fourt.railskylines.domain.response.PaymentResponse;

public interface PaymentGateway {
    PaymentResponse processPayment(double amount);
}