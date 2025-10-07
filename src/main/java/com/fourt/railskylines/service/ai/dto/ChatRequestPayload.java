package com.fourt.railskylines.service.ai.dto;

import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ChatRequestPayload(
        @NotEmpty(message = "messages are required")
        @Valid
        List<ChatMessagePayload> messages) {

    public List<ChatMessagePayload> messages() {
        return messages == null ? Collections.emptyList() : List.copyOf(messages);
    }
}
