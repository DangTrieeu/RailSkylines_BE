package com.fourt.railskylines.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cho phép các nguồn từ Next.js
        configuration.setAllowedOrigins(
                Arrays.asList(
                        "http://localhost:3000", // Next.js dev server
                        "https://railskylines-fe-1.onrender.com", "https://railskylines-fe-4.onrender.com",
                        "https://railskylines-fe-5.onrender.com", "https://railskylines-fe-6.onrender.com"));

        // Các phương thức HTTP được phép
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Các header được phép gửi lên
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // Cho phép gửi cookie hoặc thông tin xác thực
        configuration.setAllowCredentials(true);

        // Thời gian cache pre-flight request (tính bằng giây, 1 giờ = 3600 giây)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng CORS cho tất cả các endpoint
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}