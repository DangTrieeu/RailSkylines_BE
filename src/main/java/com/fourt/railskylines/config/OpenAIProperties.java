package com.fourt.railskylines.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {

    private String apiKey;
    private String baseUrl;
    private final Chat chat = new Chat();

    /**
     * Return the configured API key or fall back to the OPENAI_API_KEY environment
     * variable.
     * This lets the application pick the key up from the environment when it's not
     * present in
     * application properties (useful for local dev with a git-ignored `.env` or
     * when running in CI/Docker).
     */
    public String getApiKey() {
        if (StringUtils.hasText(this.apiKey)) {
            return this.apiKey;
        }
        String env = System.getenv("OPENAI_API_KEY");
        return StringUtils.hasText(env) ? env : null;
    }

    @Getter
    @Setter
    public static class Chat {
        private String model = "gpt-4o-mini";
        private int maxOutputTokens = 1024;
    }
}
