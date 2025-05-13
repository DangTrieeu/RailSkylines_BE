package com.fourt.railskylines.domain.response;

import com.fourt.railskylines.util.constant.CustomerObjectEnum;
import com.fourt.railskylines.util.constant.TicketStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TicketResponseDTO {
    private long ticketId;
    private CustomerObjectEnum customerObject;
    private String ticketCode;
    private String name;
    private String citizenId;
    private double price;
    private Instant startDay;
    private TicketStatusEnum ticketStatus;
    private SeatDTO seat;
    private TrainTripDTO trainTrip;

    @Getter
    @Setter
    public static class SeatDTO {
        private long seatId;
        private double price;
        private String seatStatus;
    }

    @Getter
    @Setter
    public static class TrainTripDTO {
        private long trainTripId;
        private String departure;
        private String arrival;
        private TrainDTO train;

        @Getter
        @Setter
        public static class TrainDTO {
            private long trainId;
            private String trainName;
        }
    }
}