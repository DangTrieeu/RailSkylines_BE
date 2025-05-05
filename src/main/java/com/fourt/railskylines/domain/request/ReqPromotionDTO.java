package com.fourt.railskylines.domain.request;


import com.fourt.railskylines.util.constant.PromotionStatusEnum;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class ReqPromotionDTO {
    private Long promotionId;
    private String promotionCode;
    private String promotionDescription;
    private String promotionName;
    private double discount;
    private Instant startDate; 
    private Instant validity;
    private PromotionStatusEnum status; // Thêm trường trạng thái
}