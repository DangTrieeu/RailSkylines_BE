package com.fourt.railskylines.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/", "/api/v1/auth/**", "/storage/**",
                "/api/v1/files", "/api/v1/booking", "/api/v1/callback",
                "/api/v1/callback/**",
                "/api/v1/bookings", // Thêm để bao quát các biến thể
                "/api/v1/bookings/**", // Thêm để bao quát các biến thể
                "/api/v1/tickets/search/**",
                "/api/v1/bookings/search/",
                "/api/v1/bookings/search/**",
                "/api/v1/bookings/history",
                "/api/v1/tickets/history/**",

        };
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
