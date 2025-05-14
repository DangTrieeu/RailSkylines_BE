package com.fourt.railskylines.domain.response;

import java.time.Instant;
import java.util.List;

import com.fourt.railskylines.domain.Promotion;
import com.fourt.railskylines.domain.Ticket;
import com.fourt.railskylines.util.constant.PaymentStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResBookingDTO {
    private long bookingId;
    private String bookingCode;
    private Instant date;
    private PaymentStatusEnum paymentStatus;
    private double totalPrice;
    private Instant payAt;
    private String transactionId;
    private String vnpTxnRef;
    private String paymentType;
    private ResUserDTO user;
    private String contactEmail;
    private String contactPhone;
    private Promotion promotion;
    private List<ListTickets> tickets;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ListTickets {
        private String ticketCode;
        private String citizenId;
    }
}
