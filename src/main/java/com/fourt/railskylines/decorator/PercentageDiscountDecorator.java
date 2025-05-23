package com.fourt.railskylines.decorator;

import com.fourt.railskylines.domain.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PercentageDiscountDecorator extends PriceDecorator {
    private static final Logger logger = LoggerFactory.getLogger(PercentageDiscountDecorator.class);
    private final double percentage;

    public PercentageDiscountDecorator(PriceCalculator calculator, double percentage) {
        super(calculator);
        this.percentage = percentage;
    }

    @Override
    public double calculatePrice(Booking booking) {
        double basePrice = calculator.calculatePrice(booking);
        double discountedPrice = basePrice * (1 - percentage / 100);
        logger.info("Applied {}% discount for booking {}. New price: {}", 
                    percentage, booking.getBookingCode(), discountedPrice);
        return discountedPrice;
    }
}