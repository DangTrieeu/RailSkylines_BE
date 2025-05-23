package com.fourt.railskylines.config;

import com.fourt.railskylines.repository.*;
import com.fourt.railskylines.template.BookingCreator;
import com.fourt.railskylines.template.StandardBookingCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public BookingCreator bookingCreator(
            SeatRepository seatRepository,
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            PromotionRepository promotionRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            StationRepository stationRepository,
            TrainTripRepository trainTripRepository) {
        return new StandardBookingCreator(
                seatRepository, bookingRepository, ticketRepository,
                promotionRepository, userRepository, objectMapper,
                stationRepository, trainTripRepository);
    }
}