package com.fourt.railskylines.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {

    private String apiKey;
    private String baseUrl;
    private final Chat chat = new Chat();

    @Getter
    @Setter
    public static class Chat {
        private String model = "gpt-4o-mini";
        private int maxOutputTokens = 1024;
    }
}
