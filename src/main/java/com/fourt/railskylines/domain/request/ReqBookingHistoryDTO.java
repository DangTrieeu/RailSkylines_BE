package com.fourt.railskylines.domain.request;

import java.time.Instant;
import java.util.List;

import com.fourt.railskylines.util.constant.PaymentStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqBookingHistoryDTO {
    private String bookingCode;
    private PaymentStatusEnum paymentStatus;
    private Instant date;
    private Double totalPrice;
    private String paymentType;
    private String contactEmail;
    private String contactPhone;
    private List<ReqTicketHistoryDTO> tickets;
}