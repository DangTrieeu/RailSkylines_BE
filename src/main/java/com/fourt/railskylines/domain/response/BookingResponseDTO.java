package com.fourt.railskylines.domain.response;

import com.fourt.railskylines.util.constant.PaymentStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BookingResponseDTO {
    private long bookingId;
    private String bookingCode;
    private Instant date;
    private PaymentStatusEnum paymentStatus;
    private double totalPrice;
    private Instant payAt;
    private String transactionId;
    private String vnpTxnRef;
    private String paymentType;
    private String contactEmail;
    private String contactPhone;
    private List<TicketResponseDTO> tickets;
    private List<PromotionDTO> promotions;

    @Getter
    @Setter
    public static class PromotionDTO {
        private long promotionId;
        private double discount;
        // Add other promotion fields if needed
    }
}