package com.fourt.railskylines.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fourt.railskylines.domain.Permission;
import com.fourt.railskylines.domain.Role;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.repository.PermissionRepository;
import com.fourt.railskylines.repository.RoleRepository;
import com.fourt.railskylines.repository.UserRepository;

@Service
public class DatabaseInitializer implements CommandLineRunner {
        private final PermissionRepository permissionRepository;
        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public DatabaseInitializer(
                        PermissionRepository permissionRepository,
                        RoleRepository roleRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
                this.permissionRepository = permissionRepository;
                this.roleRepository = roleRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) throws Exception {
                System.out.println(">>> START INIT DATABASE");
                long countPermissions = this.permissionRepository.count();
                long countRoles = this.roleRepository.count();
                long countUsers = this.userRepository.count();

                if (countPermissions == 0) {
                        ArrayList<Permission> arr = new ArrayList<>();

                        // Article Permissions
                        arr.add(new Permission("Create an article", "/api/v1/articles", "POST", "ARTICLES"));
                        arr.add(new Permission("Update an article", "/api/v1/articles/{id}", "PUT", "ARTICLES"));
                        arr.add(new Permission("Delete an article", "/api/v1/articles/{id}", "DELETE", "ARTICLES"));
                        arr.add(new Permission("Get an article by id", "/api/v1/articles/{id}", "GET", "ARTICLES"));
                        arr.add(new Permission("Get articles with pagination", "/api/v1/articles", "GET", "ARTICLES"));

                        // Booking Permissions
                        arr.add(new Permission("Create a booking", "/api/v1/bookings", "POST", "BOOKINGS"));
                        arr.add(new Permission("Update a booking", "/api/v1/bookings/{id}", "PUT", "BOOKINGS"));
                        arr.add(new Permission("Delete a booking", "/api/v1/bookings/{id}", "DELETE", "BOOKINGS"));
                        arr.add(new Permission("Get a booking by id", "/api/v1/bookings/{id}", "GET", "BOOKINGS"));
                        arr.add(new Permission("Get bookings with pagination", "/api/v1/bookings", "GET", "BOOKINGS"));

                        // Carriage Permissions
                        arr.add(new Permission("Create a carriage", "/api/v1/carriages", "POST", "CARRIAGES"));
                        arr.add(new Permission("Update a carriage", "/api/v1/carriages/{id}", "PUT", "CARRIAGES"));
                        arr.add(new Permission("Delete a carriage", "/api/v1/carriages/{id}", "DELETE", "CARRIAGES"));
                        arr.add(new Permission("Get a carriage by id", "/api/v1/carriages/{id}", "GET", "CARRIAGES"));
                        arr.add(new Permission("Get carriages with pagination", "/api/v1/carriages", "GET",
                                        "CARRIAGES"));
                        arr.add(new Permission("Get seats with carriage id", "/api/v1/carriages/seat/{id}", "GET",
                                        "CARRIAGES"));

                        // ClockTime Permissions
                        arr.add(new Permission("Create a clock time", "/api/v1/clock-times", "POST", "CLOCK_TIMES"));
                        arr.add(new Permission("Update a clock time", "/api/v1/clock-times/{id}", "PUT",
                                        "CLOCK_TIMES"));
                        arr.add(new Permission("Delete a clock time", "/api/v1/clock-times/{id}", "DELETE",
                                        "CLOCK_TIMES"));
                        arr.add(new Permission("Get a clock time by id", "/api/v1/clock-times/{id}", "GET",
                                        "CLOCK_TIMES"));
                        arr.add(new Permission("Get clock times with pagination", "/api/v1/clock-times", "GET",
                                        "CLOCK_TIMES"));

                        // Permission Permissions
                        arr.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
                        arr.add(new Permission("Update a permission", "/api/v1/permissions/{id}", "PUT",
                                        "PERMISSIONS"));
                        arr.add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE",
                                        "PERMISSIONS"));
                        arr.add(new Permission("Get a permission by id", "/api/v1/permissions/{id}", "GET",
                                        "PERMISSIONS"));
                        arr.add(new Permission("Get permissions with pagination", "/api/v1/permissions", "GET",
                                        "PERMISSIONS"));

                        // Promotion Permissions
                        arr.add(new Permission("Create a promotion", "/api/v1/promotions", "POST", "PROMOTIONS"));
                        arr.add(new Permission("Update a promotion", "/api/v1/promotions/{id}", "PUT", "PROMOTIONS"));
                        arr.add(new Permission("Delete a promotion", "/api/v1/promotions/{id}", "DELETE",
                                        "PROMOTIONS"));
                        arr.add(new Permission("Get a promotion by id", "/api/v1/promotions/{id}", "GET",
                                        "PROMOTIONS"));
                        arr.add(new Permission("Get promotions with pagination", "/api/v1/promotions", "GET",
                                        "PROMOTIONS"));

                        // Role Permissions
                        arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
                        arr.add(new Permission("Update a role", "/api/v1/roles/{id}", "PUT", "ROLES"));
                        arr.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
                        arr.add(new Permission("Get a role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
                        arr.add(new Permission("Get roles with pagination", "/api/v1/roles", "GET", "ROLES"));

                        // Route Permissions
                        arr.add(new Permission("Create a route", "/api/v1/routes", "POST", "ROUTES"));
                        arr.add(new Permission("Update a route", "/api/v1/routes/{id}", "PUT", "ROUTES"));
                        arr.add(new Permission("Delete a route", "/api/v1/routes/{id}", "DELETE", "ROUTES"));
                        arr.add(new Permission("Get a route by id", "/api/v1/routes/{id}", "GET", "ROUTES"));
                        arr.add(new Permission("Get routes with pagination", "/api/v1/routes", "GET", "ROUTES"));

                        // Schedule Permissions
                        arr.add(new Permission("Create a schedule", "/api/v1/schedules", "POST", "SCHEDULES"));
                        arr.add(new Permission("Update a schedule", "/api/v1/schedules/{id}", "PUT", "SCHEDULES"));
                        arr.add(new Permission("Delete a schedule", "/api/v1/schedules/{id}", "DELETE", "SCHEDULES"));
                        arr.add(new Permission("Get a schedule by id", "/api/v1/schedules/{id}", "GET", "SCHEDULES"));
                        arr.add(new Permission("Get schedules with pagination", "/api/v1/schedules", "GET",
                                        "SCHEDULES"));

                        // Seat Permissions
                        arr.add(new Permission("Create a seat", "/api/v1/seats", "POST", "SEATS"));
                        arr.add(new Permission("Update a seat", "/api/v1/seats/{id}", "PUT", "SEATS"));
                        arr.add(new Permission("Delete a seat", "/api/v1/seats/{id}", "DELETE", "SEATS"));
                        arr.add(new Permission("Get a seat by id", "/api/v1/seats/{id}", "GET", "SEATS"));
                        arr.add(new Permission("Get seats with pagination", "/api/v1/seats", "GET", "SEATS"));

                        // Station Permissions
                        arr.add(new Permission("Create a station", "/api/v1/stations", "POST", "STATIONS"));
                        arr.add(new Permission("Update a station", "/api/v1/stations/{id}", "PUT", "STATIONS"));
                        arr.add(new Permission("Delete a station", "/api/v1/stations/{id}", "DELETE", "STATIONS"));
                        arr.add(new Permission("Get a station by id", "/api/v1/stations/{id}", "GET", "STATIONS"));
                        arr.add(new Permission("Get stations with pagination", "/api/v1/stations", "GET", "STATIONS"));

                        // Ticket Permissions
                        arr.add(new Permission("Create a ticket", "/api/v1/tickets", "POST", "TICKETS"));
                        arr.add(new Permission("Update a ticket", "/api/v1/tickets/{id}", "PUT", "TICKETS"));
                        arr.add(new Permission("Delete a ticket", "/api/v1/tickets/{id}", "DELETE", "TICKETS"));
                        arr.add(new Permission("Get a ticket by id", "/api/v1/tickets/{id}", "GET", "TICKETS"));
                        arr.add(new Permission("Get tickets with pagination", "/api/v1/tickets", "GET", "TICKETS"));

                        // Train Permissions
                        arr.add(new Permission("Create a train", "/api/v1/trains", "POST", "TRAINS"));
                        arr.add(new Permission("Update a train", "/api/v1/trains/{id}", "PUT", "TRAINS"));
                        arr.add(new Permission("Delete a train", "/api/v1/trains/{id}", "DELETE", "TRAINS"));
                        arr.add(new Permission("Get a train by id", "/api/v1/trains/{id}", "GET", "TRAINS"));
                        arr.add(new Permission("Get trains with pagination", "/api/v1/trains", "GET", "TRAINS"));

                        // TrainTrip Permissions
                        arr.add(new Permission("Create a train trip", "/api/v1/train-trips", "POST", "TRAIN_TRIPS"));
                        arr.add(new Permission("Update a train trip", "/api/v1/train-trips/{id}", "PUT",
                                        "TRAIN_TRIPS"));
                        arr.add(new Permission("Delete a train trip", "/api/v1/train-trips/{id}", "DELETE",
                                        "TRAIN_TRIPS"));
                        arr.add(new Permission("Get a train trip by id", "/api/v1/train-trips/{id}", "GET",
                                        "TRAIN_TRIPS"));
                        arr.add(new Permission("Get carriages by train trip by id",
                                        "/api/v1/train-trips/{id}/carriages", "GET",
                                        "TRAIN_TRIPS"));
                        arr.add(new Permission("Get train trips with pagination", "/api/v1/train-trips", "GET",
                                        "TRAIN_TRIPS"));

                        // User Permissions
                        arr.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
                        arr.add(new Permission("Update a user", "/api/v1/users/{id}", "PUT", "USERS"));
                        arr.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
                        arr.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET", "USERS"));
                        arr.add(new Permission("Get users with pagination", "/api/v1/users", "GET", "USERS"));

                        this.permissionRepository.saveAll(arr);
                }

                if (countRoles == 0) {
                        List<Permission> allPermissions = this.permissionRepository.findAll();

                        // SUPER_ADMIN Role
                        Role adminRole = new Role();
                        adminRole.setName("SUPER_ADMIN");
                        adminRole.setDescription("Super admin with full permissions");
                        adminRole.setActive(true);
                        adminRole.setPermissions(allPermissions);

                        // NORMAL_USER Role
                        Role normalUserRole = new Role();
                        normalUserRole.setName("NORMAL_USER");
                        normalUserRole.setDescription("Normal user with read-only access to public resources");
                        normalUserRole.setActive(true);

                        // Filter permissions for NORMAL_USER
                        List<Permission> normalUserPermissions = allPermissions.stream()
                                        .filter(p -> (p.getApiPath().equals("/api/v1/articles")
                                                        && p.getMethod().equals("GET")
                                                        && p.getModule().equals("ARTICLES")) ||
                                                        (p.getApiPath().equals("/api/v1/articles/{id}")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("ARTICLES"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/carriages/{id}")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("CARRIAGES"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/seats/{id}")
                                                                        && p.getMethod().equals("PUT")
                                                                        && p.getModule().equals("SEATS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/carriages")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("CARRIAGES"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/carriages/seat/{id}")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("CARRIAGES"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/promotions/{id}")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("PROMOTIONS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/promotions")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("PROMOTIONS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/stations/{id}")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("STATIONS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/stations")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("STATIONS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/trains/{id}")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("TRAINS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/trains")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("TRAINS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/train-trips/{id}/carriages")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("TRAIN_TRIPS"))
                                                        ||
                                                        (p.getApiPath().equals("/api/v1/train-trips")
                                                                        && p.getMethod().equals("GET")
                                                                        && p.getModule().equals("TRAIN_TRIPS")))
                                        .collect(Collectors.toList());
                        normalUserRole.setPermissions(normalUserPermissions);

                        // Save both roles
                        this.roleRepository.saveAll(List.of(adminRole, normalUserRole));
                }

                if (countUsers == 0) {
                        // Admin User
                        User adminUser = new User();
                        adminUser.setEmail("admin@railskylines.com");
                        adminUser.setFullName("Super Admin");
                        adminUser.setPassword(this.passwordEncoder.encode("20102007"));
                        adminUser.setCreatedAt(Instant.now());
                        adminUser.setCreatedBy("system");

                        Role adminRole = this.roleRepository.findByName("SUPER_ADMIN");
                        if (adminRole != null) {
                                adminUser.setRole(adminRole);
                        }

                        // Normal User (optional, for testing)
                        User normalUser = new User();
                        normalUser.setEmail("user@railskylines.com");
                        normalUser.setFullName("User Normal");
                        normalUser.setPassword(this.passwordEncoder.encode("20102007"));
                        normalUser.setCreatedAt(Instant.now());
                        normalUser.setCreatedBy("system");

                        Role normalUserRole = this.roleRepository.findByName("NORMAL_USER");
                        if (normalUserRole != null) {
                                normalUser.setRole(normalUserRole);
                        }

                        // Save both users
                        this.userRepository.saveAll(List.of(adminUser, normalUser));
                }

                if (countPermissions > 0 && countRoles > 0 && countUsers > 0) {
                        System.out.println(">>> SKIP INIT DATABASE ~ ALREADY HAVE DATA...");
                } else {
                        System.out.println(">>> END INIT DATABASE");
                }
        }
}