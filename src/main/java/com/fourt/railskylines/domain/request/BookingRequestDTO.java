package com.fourt.railskylines.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingRequestDTO {
    private List<Long> seatIds;

    private Long userId;

    @Email
    private String contactEmail;

    private String contactPhone;

    private Long promotionId;
    
    @NotEmpty
    private List<TicketRequestDTO> tickets;

    private String paymentType;

    private String ticketsParam;

    private Long trainTripId;
}