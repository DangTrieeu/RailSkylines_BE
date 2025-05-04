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
        // String[] whiteList = {
        // "/", "/api/v1/auth/**", "/storage/**",
        // "/api/v1/files",

        // };
        String[] whiteList = {
                "/", "/api/v1/auth/**", "/storage/**",
                "/api/v1/trains/**", "/api/v1/carriages/**", "/api/v1/routes/**",
                "/api/v1/files",
                "/api/v1/seats/**",
                "/api/v1/stations/**",
                "/api/v1/schedules/**",
                "/api/v1/promotions/**",
                "/api/v1/articles/**",

        };
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
