package com.fourt.railskylines.decorator;

import com.fourt.railskylines.domain.Booking;

public interface PriceCalculator {
    double calculatePrice(Booking booking);
}