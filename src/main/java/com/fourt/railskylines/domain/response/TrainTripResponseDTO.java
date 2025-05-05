// // package com.fourt.railskylines.domain.response;

// // import com.fourt.railskylines.domain.*;
// // import com.fourt.railskylines.util.constant.TrainStatusEnum;
// // import lombok.Data;

// // import java.util.List;

// // @Data
// // public class TrainTripResponseDTO {
// //     private Long trainTripId;
// //     private TrainDTO train;
// //     private Route route;
// //     private Schedule schedule;

// //     @Data
// //     public static class TrainDTO {
// //         private Long trainId;
// //         private String trainName;
// //         private TrainStatusEnum trainStatus;
// //         private List<CarriageDTO> carriages;
// //     }

// //     @Data
// //     public static class CarriageDTO {
// //         private Long carriageId;
// //         private String carriageType;
// //         private Double price;
// //         private Double discount;
// //         private TrainDTO train;
// //     }
// // }
// package com.fourt.railskylines.domain.response;

// import com.fourt.railskylines.domain.*;
// import com.fourt.railskylines.util.constant.TrainStatusEnum;
// import lombok.Data;

// import java.util.List;

// @Data
// public class TrainTripResponseDTO {
//     private Long trainTripId;
//     private TrainDTO train;
//     private Route route;
//     private Schedule schedule;

//     @Data
//     public static class TrainDTO {
//         private Long trainId;
//         private String trainName;
//         private TrainStatusEnum trainStatus;
//         private List<CarriageDTO> carriages;
//     }

//     @Data
//     public static class SimpleTrainDTO {
//         private Long trainId;
//         private String trainName;
//         private TrainStatusEnum trainStatus;
//     }

//     @Data
//     public static class CarriageDTO {
//         private Long carriageId;
//         private String carriageType;
//         private Double price;
//         private Double discount;
//         private SimpleTrainDTO train;
//     }
// }

// package com.fourt.railskylines.domain.response;

// import com.fourt.railskylines.domain.*;
// import com.fourt.railskylines.util.constant.SeatStatusEnum;
// import com.fourt.railskylines.util.constant.SeatTypeEnum;
// import com.fourt.railskylines.util.constant.TrainStatusEnum;
// import lombok.Data;

// import java.util.List;

// @Data
// public class TrainTripResponseDTO {
//     private Long trainTripId;
//     private TrainDTO train;
//     private Route route;
//     private Schedule schedule;

//     @Data
//     public static class TrainDTO {
//         private Long trainId;
//         private String trainName;
//         private TrainStatusEnum trainStatus;
//         private List<CarriageDTO> carriages;
//     }

//     @Data
//     public static class SimpleTrainDTO {
//         private Long trainId;
//         private String trainName;
//         private TrainStatusEnum trainStatus;
//     }

//     @Data
//     public static class CarriageDTO {
//         private Long carriageId;
//         private String carriageType;
//         private Double price;
//         private Double discount;
//         private SimpleTrainDTO train;
//         private List<SeatDTO> seats;
//     }

//     @Data
//     public static class SeatDTO {
//         private Long seatId;
//         private Integer seatNumber;
//         private SeatTypeEnum seatType;
//         private SeatStatusEnum seatStatus;
//         private Double price;
//     }
// }
package com.fourt.railskylines.domain.response;

import com.fourt.railskylines.domain.*;
import com.fourt.railskylines.util.constant.SeatStatusEnum;
import com.fourt.railskylines.util.constant.SeatTypeEnum;
import com.fourt.railskylines.util.constant.TrainStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class TrainTripResponseDTO {
    private Long trainTripId;
    private TrainDTO train;
    private Route route;
    private Schedule schedule;

    @Data
    public static class TrainDTO {
        private Long trainId;
        private String trainName;
        private TrainStatusEnum trainStatus;
        private List<CarriageDTO> carriages;
    }

    @Data
    public static class SimpleTrainDTO {
        private Long trainId;
        private String trainName;
        private TrainStatusEnum trainStatus;
    }

    @Data
    public static class CarriageDTO {
        private Long carriageId;
        private String carriageType;
        private Double price;
        private Double discount;
        private SimpleTrainDTO train;
        private List<SeatDTO> seats;
    }

    @Data
    public static class SeatDTO {
        private Long seatId;
        private Integer seatNumber;
        private SeatTypeEnum seatType;
        private SeatStatusEnum seatStatus;
        private Double price;
    }
}