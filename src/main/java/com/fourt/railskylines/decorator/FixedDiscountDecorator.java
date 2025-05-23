package com.fourt.railskylines.decorator;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Promotion;
import com.fourt.railskylines.util.constant.PromotionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class FixedDiscountDecorator extends PriceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(FixedDiscountDecorator.class);
    private final Promotion promotion;

    public FixedDiscountDecorator(PriceCalculator calculator, Promotion promotion) {
        super(calculator);
        this.promotion = promotion;
    }

    @Override
    public double calculatePrice(Booking booking) {
        double basePrice = calculator.calculatePrice(booking);
        if (promotion.getValidity().isAfter(Instant.now()) && promotion.getStatus() == PromotionStatusEnum.active) {
            double discountedPrice = Math.max(0, basePrice - promotion.getDiscount());
            logger.info("Applied fixed discount of {} for booking {}. New price: {}", 
                        promotion.getDiscount(), booking.getBookingCode(), discountedPrice);
            return discountedPrice;
        }
        logger.warn("Promotion {} is not valid for booking {}", 
                    promotion.getPromotionCode(), booking.getBookingCode());
        return basePrice;
    }
}