package com.fourt.railskylines.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fourt.railskylines.service.ai.dto.ChatRequestPayload;

import jakarta.validation.Valid;

import java.io.IOException;

@RestController
@RequestMapping("/api/debug")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping(value = "/chat-test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testChat(@Valid @RequestBody ChatRequestPayload request) {
        SseEmitter emitter = new SseEmitter(30000L); // 30 seconds timeout

        try {
            // Send a simple test response
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data("Xin chào! Đây là response test từ RailSkylines chatbot."));

            emitter.send(SseEmitter.event()
                    .name("done")
                    .data(""));

            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}