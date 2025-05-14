package com.fourt.railskylines.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResRevenueDTO {
    private double totalRevenue;
    private long totalCustomers;
    private long paidTickets;
    private long pendingTickets;
    private List<RevenueByDate> revenueByDate;
    private List<TrainRanking> trainRankings;

    @Getter
    @Setter
    public static class RevenueByDate {
        private String date; // Định dạng dd/MM/yyyy
        private double revenue;
    }

    @Getter
    @Setter
    public static class TrainRanking {
        private String name; // Tên tàu
        private long successOrders; // Số vé đã thanh toán thành công
        private String fill; // Màu sắc cho biểu đồ
    }
}