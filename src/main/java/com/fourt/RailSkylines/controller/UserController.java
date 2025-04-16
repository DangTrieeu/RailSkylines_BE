package com.fourt.RailSkylines.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.RailSkylines.domain.User;
import com.fourt.RailSkylines.service.UserService;

@RestController
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }
    // @GetMapping("/users/create")
    @PostMapping("/users")
    public ResponseEntity<User> CreateNewUser(@RequestBody User PostmanUser) {
        String hashPassword = passwordEncoder.encode(PostmanUser.getPassword());
        PostmanUser.setPassword(hashPassword);
        User toanUser = userService.handleCreateUser(PostmanUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toanUser);
    }
    
    @DeleteMapping("/users/{id}")
        public ResponseEntity<String> DeleteUser(@PathVariable("id") long id) {
            this.userService.handleDeleteUser(id);
            return ResponseEntity.status(HttpStatus.OK).body("Delete user with id: " + id + " success!");
        }
    @GetMapping("/users/{id}")
        public ResponseEntity<User> GetUser(@PathVariable("id") long id) {
            return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetUser(id));
        }
    @GetMapping("/users")
        public ResponseEntity<List<User>> GetAllUser() {
            return ResponseEntity.status(HttpStatus.OK).body(this.userService.handleGetAllUser());
        }
    @PutMapping("/users")
        public ResponseEntity<User> UpdateUser(@RequestBody User PostmanUser) {
            User toanUser = userService.handleUpdateUser(PostmanUser);
            return ResponseEntity.status(HttpStatus.OK).body(toanUser);
        }
}
