<<<<<<< HEAD

package com.fourt.railskylines.config;

import java.util.Arrays;

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
                "/",
                "/api/v1/auth/**",
                "/storage/**",
                "/api/v1/files",
                "/api/v1/bookings",
                "/api/v1/bookings/**",
                "/api/v1/callback",
                "/api/v1/callback/**",
                "/api/v1/tickets/**",
                "/api/v1/vn-pay",
                "/api/v1/vn-pay/**",
                "/api/v1/train-trips/**",
                "/api/v1/trains/**"
        };
        System.out.println(">>> Registering PermissionInterceptor with exclusions: " + Arrays.toString(whiteList));
        registry.addInterceptor(getPermissionInterceptor())
                .addPathPatterns("/**") // Explicitly include all paths
                .excludePathPatterns(whiteList);
    }
}
=======
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
                "/api/v1/files",
                "/api/v1/vn-pay",
                "/api/v1/vn-pay/**", // Thêm để bao quát các biến thể
                "/api/v1/callback",
                "/api/v1/callback/**",
                "/api/v1/bookings" ,// Thêm để bao quát các biến thể
                "/api/v1/bookings/**" // Thêm để bao quát các biến thể

        };
        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
>>>>>>> 7d4cc6489dc9ebcdb7015abfb800f91f72e093cb
