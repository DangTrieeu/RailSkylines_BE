package com.fourt.railskylines.decorator;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasePriceCalculator implements PriceCalculator {
    private static final Logger logger = LoggerFactory.getLogger(BasePriceCalculator.class);

    @Override
    public double calculatePrice(Booking booking) {
        double totalPrice = booking.getTickets().stream()
                .mapToDouble(Ticket::getPrice)
                .sum();
        logger.info("Calculated base price for booking {}: {}", booking.getBookingCode(), totalPrice);
        return totalPrice;
    }
}