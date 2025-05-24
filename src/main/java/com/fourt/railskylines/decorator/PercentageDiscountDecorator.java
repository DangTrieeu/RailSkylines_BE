package com.fourt.railskylines.decorator;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Promotion;
import com.fourt.railskylines.util.constant.PromotionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class PercentageDiscountDecorator extends PriceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(PercentageDiscountDecorator.class);
    private final Promotion promotion;

    public PercentageDiscountDecorator(PriceCalculator calculator, Promotion promotion) {
        super(calculator);
        this.promotion = promotion;
    }

    @Override
    public double calculatePrice(Booking booking) {
        double basePrice = calculator.calculatePrice(booking);
        logger.info("Base price for booking {}: {}", booking.getBookingCode(), basePrice);
        logger.info("Percentage discount for promotion {}: {}%", promotion.getPromotionCode(), promotion.getDiscount());

        if (promotion.getValidity().isAfter(Instant.now()) && promotion.getStatus() == PromotionStatusEnum.active) {
            double discountPercentage = promotion.getDiscount();
            double discountAmount = basePrice * (discountPercentage / 100.0);
            double discountedPrice = Math.max(0, Math.round((basePrice - discountAmount) * 100.0) / 100.0);
            logger.info("Applied percentage discount of {}% (amount: {}) for booking {}. New price: {}", 
                        discountPercentage, discountAmount, booking.getBookingCode(), discountedPrice);
            return discountedPrice;
        }
        logger.warn("Promotion {} is not valid for booking {}", 
                    promotion.getPromotionCode(), booking.getBookingCode());
        return basePrice;
    }
}