package com.fourt.railskylines.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class SimpleTestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong - test endpoint working!";
    }

    @PostMapping("/chat-simple")
    public String chatTest() {
        return "Chat functionality test - server is responding!";
    }
}