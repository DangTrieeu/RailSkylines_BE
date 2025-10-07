package com.fourt.railskylines.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fourt.railskylines.service.ai.ChatService;
import com.fourt.railskylines.service.ai.dto.ChatRequestPayload;

import jakarta.validation.Valid;

/**
 * REST controller for chat functionality with AI chatbot
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Stream chat responses using Server-Sent Events
     * 
     * @param request Chat request containing message history
     * @return SseEmitter for streaming response
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody ChatRequestPayload request) {
        return chatService.streamChat(request);
    }
}