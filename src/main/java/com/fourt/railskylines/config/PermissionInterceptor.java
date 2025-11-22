<<<<<<< HEAD
package com.fourt.railskylines.config;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.fourt.railskylines.domain.Permission;
import com.fourt.railskylines.domain.Role;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.service.UserService;
import com.fourt.railskylines.util.SecurityUtil;
import com.fourt.railskylines.util.error.PermissionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    private static final List<String> WHITELIST = Arrays.asList(
            "/",
            "/api/v1/auth/**",
            "/storage/**",
            "/api/v1/files",
            "/api/v1/vn-pay",
            "/api/v1/bookings/**",
            "/api/v1/bookings",
            "/api/v1/callback",
            "/api/v1/tickets/**",
            "/api/v1/vn-pay",
            "/api/v1/vn-pay/**",
            "/api/v1/callback/**",
            "/api/v1/train-trips/**",
            "/api/v1/trains/**",
            "/api/v1/seats/**"

    );

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);
        String normalizedURI = requestURI.split("\\?")[0];

        // Specific bypass for POST /api/v1/bookings
        if (normalizedURI.equals("/api/v1/bookings") && "POST".equalsIgnoreCase(httpMethod)) {
            System.out.println(">>> Bypassing permission check for POST /api/v1/bookings");
            return true;
        }
        if (requestURI.startsWith("/api/v1/articles") && httpMethod.equals("GET")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/articles/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/promotions") &&
                httpMethod.equals("GET")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/promotions/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/seats/**") && httpMethod.equals("PUT")) {
            System.out.println(">>> Bypassing permission check for PUT /api/v1/seats/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/stations") && httpMethod.equals("GET")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/stations/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/train-trips/**") && httpMethod.equals("GET")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/train-trips/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/trains/**") && httpMethod.equals("GET")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/trains/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/carriages/**") && httpMethod.equals("GET")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/trains/**");
            return true;
        }
        if (requestURI.startsWith("/api/v1/bookings") && httpMethod.equals("POST")) {
            System.out.println(">>> Bypassing permission check for GET /api/v1/trains/**");
            return true;
        }
        // Kiểm tra xem endpoint có nằm trong whitelist không
        boolean isWhitelisted = WHITELIST.stream().anyMatch(whitelistPath -> {
            if (whitelistPath.endsWith("/**")) {
                String prefix = whitelistPath.substring(0, whitelistPath.length() - 3);
                return requestURI.startsWith(prefix);
            }

            return requestURI.equals(whitelistPath);
        });

        if (isWhitelisted) {
            System.out.println(">>> Bypassing permission check for whitelisted endpoint: " + requestURI);
            return true; // Bỏ qua kiểm tra quyền
        }

        // Check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (!email.isEmpty()) {
            User user = this.userService.handleGetUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream()
                            .anyMatch(item -> item.getApiPath().equals(path) && item.getMethod().equals(httpMethod));

                    if (!isAllow) {
                        throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                    }
                } else {
                    throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                }
            }
        } else {
            throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
        }

        return true;
    }
}
=======
package com.fourt.railskylines.config;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.fourt.railskylines.domain.Permission;
import com.fourt.railskylines.domain.Role;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.service.UserService;
import com.fourt.railskylines.util.SecurityUtil;
import com.fourt.railskylines.util.error.PermissionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// import vn.hoidanit.jobhunter.domain.Permission;
// import vn.hoidanit.jobhunter.domain.Role;
// import vn.hoidanit.jobhunter.domain.User;
// import vn.hoidanit.jobhunter.service.UserService;
// import vn.hoidanit.jobhunter.util.SecurityUtil;
// import vn.hoidanit.jobhunter.util.error.PermissionException;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    private static final List<String> WHITELIST = Arrays.asList(
            "/",
            "/api/v1/auth/**",
            "/storage/**",
            "/api/v1/files",
            "/api/v1/vn-pay");

    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        // Kiểm tra xem endpoint có nằm trong whitelist không
        boolean isWhitelisted = WHITELIST.stream().anyMatch(whitelistPath -> {
            if (whitelistPath.endsWith("/**")) {
                String prefix = whitelistPath.substring(0, whitelistPath.length() - 3);
                return requestURI.startsWith(prefix);
            }
            return requestURI.equals(whitelistPath);
        });

        if (isWhitelisted) {
            System.out.println(">>> Bypassing permission check for whitelisted endpoint: " + requestURI);
            return true; // Bỏ qua kiểm tra quyền
        }

        // Check permission
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";
        if (!email.isEmpty()) {
            User user = this.userService.handleGetUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow = permissions.stream()
                            .anyMatch(item -> item.getApiPath().equals(path) && item.getMethod().equals(httpMethod));

                    if (!isAllow) {
                        throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                    }
                } else {
                    throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
                }
            }
        } else {
            throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
        }

        return true;
    }
}
>>>>>>> 7d4cc6489dc9ebcdb7015abfb800f91f72e093cb
