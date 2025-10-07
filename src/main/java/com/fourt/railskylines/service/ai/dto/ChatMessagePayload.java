package com.fourt.railskylines.service.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessagePayload(
        @NotBlank(message = "role is required") String role,
        @NotBlank(message = "content is required") String content) {

    public ChatMessagePayload {
        role = role == null ? "" : role.trim();
        content = content == null ? "" : content;
    }

    public boolean isUser() {
        return "user".equalsIgnoreCase(role);
    }
}
