package com.fourt.railskylines.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "chatbot")
public class ChatbotProperties {

    private final Embedding embedding = new Embedding();

    @Getter
    @Setter
    public static class Embedding {
        private String modelId = "djl://ai.djl.huggingface/sentence-transformers/all-MiniLM-L6-v2";
        private int maxSources = 3;
    }
}
