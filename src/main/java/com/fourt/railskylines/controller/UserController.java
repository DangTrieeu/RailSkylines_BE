package com.fourt.railskylines.controller;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.domain.response.ResCreateUserDTO;
import com.fourt.railskylines.domain.response.ResUpdateUserDTO;
import com.fourt.railskylines.domain.response.ResUserDTO;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.service.UserService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Create a new user
    @PostMapping("/users")
    @APIMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User newUser)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(newUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + newUser.getEmail() + " already exists, please use another email.");
        }

        String hashPassword = this.passwordEncoder.encode(newUser.getPassword());
        newUser.setPassword(hashPassword);
        User user = this.userService.handleCreateNewUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(user));
    }

    // fetch user by id
    @GetMapping("/users/{userId}")
    @APIMessage("Fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("userId") long id) throws IdInvalidException {
        User fetchUser = this.userService.handleFetchUserById(id);
        if (fetchUser == null) {
            throw new IdInvalidException("User with id = " + id + " does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.userService.convertToResUserDTO(fetchUser));
    }

    // fetch all users
    @GetMapping("/users")
    @APIMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            @Filter Specification<User> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.handleFetchAllUser(spec, pageable));
    }

    // update user by id
    @PutMapping("/users/{id}")
    @APIMessage("Update user by id")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@PathVariable("id") long id, @RequestBody User updateUser)
            throws IdInvalidException {
        if (this.userService.handleFetchUserById(id) == null) {
            throw new IdInvalidException("User with id = " + id + " does not exist");
        }
        User user = this.userService.handleUpdateUser(id, updateUser);
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(user));
    }

    // delete user by id
    @DeleteMapping("/users/{id}")
    @APIMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id)
            throws IdInvalidException {
        User currentUser = this.userService.handleFetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User with id = " + id + " does not exist");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }
}
