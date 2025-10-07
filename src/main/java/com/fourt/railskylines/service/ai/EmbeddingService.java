package com.fourt.railskylines.service.ai;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fourt.railskylines.config.ChatbotProperties;
import com.fourt.railskylines.config.OpenAIProperties;
import com.fourt.railskylines.domain.Article;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddingService.class);
    private static final okhttp3.MediaType JSON = okhttp3.MediaType.get(MediaType.APPLICATION_JSON_VALUE);

    private final ChatbotProperties chatbotProperties;
    private final OpenAIProperties openAIProperties;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public EmbeddingService(
            ChatbotProperties chatbotProperties,
            OpenAIProperties openAIProperties,
            ObjectMapper objectMapper) {
        this.chatbotProperties = chatbotProperties;
        this.openAIProperties = openAIProperties;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder().build();
    }

    public List<Double> embedArticle(Article article) {
        if (article == null) {
            return Collections.emptyList();
        }
        String combined = Stream
                .of(article.getTitle(), article.getContent(), article.getThumbnail())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining("\n\n"));
        if (!StringUtils.hasText(combined)) {
            return Collections.emptyList();
        }
        try {
            return embedText(combined);
        } catch (EmbeddingException exception) {
            LOGGER.warn("Failed to generate embedding for article {}: {}", article.getArticleId(), exception.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Double> embedText(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        String model = chatbotProperties.getEmbedding().getModelId();
        if (!StringUtils.hasText(model)) {
            LOGGER.debug("Embedding model id is empty; skipping embedding generation.");
            return Collections.emptyList();
        }
        if (!StringUtils.hasText(openAIProperties.getApiKey())) {
            LOGGER.debug("OpenAI API key missing; unable to call embedding endpoint.");
            return Collections.emptyList();
        }
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.put("input", text.trim());

        Request request = new Request.Builder()
                .url(resolveEmbeddingsEndpoint())
                .addHeader("Authorization", "Bearer " + openAIProperties.getApiKey())
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .post(RequestBody.create(payload.toString(), JSON))
                .build();
        try {
            return executeEmbeddingRequest(request);
        } catch (EmbeddingException exception) {
            LOGGER.warn("Embedding request failed: {}", exception.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Double> executeEmbeddingRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String message = response.body() != null ? response.body().string() : "empty response";
                throw new EmbeddingException("Embedding request failed: " + response.code() + " " + message);
            }
            JsonNode root = objectMapper.readTree(response.body().string());
            JsonNode data = root.path("data");
            if (!data.isArray() || data.isEmpty()) {
                throw new EmbeddingException("Embedding response missing data array");
            }
            JsonNode embeddingNode = data.get(0).path("embedding");
            if (!embeddingNode.isArray()) {
                throw new EmbeddingException("Embedding response malformed (embedding array missing)");
            }
            double[] vector = objectMapper.convertValue(embeddingNode, double[].class);
            return toDoubleList(vector);
        } catch (IOException exception) {
            throw new EmbeddingException("Failed to call OpenAI embedding endpoint", exception);
        }
    }

    public double cosineSimilarity(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0d;
        }
        int length = Math.min(left.size(), right.size());
        double dot = 0d;
        double leftMagnitude = 0d;
        double rightMagnitude = 0d;
        for (int index = 0; index < length; index++) {
            double l = left.get(index);
            double r = right.get(index);
            dot += l * r;
            leftMagnitude += l * l;
            rightMagnitude += r * r;
        }
        if (leftMagnitude == 0d || rightMagnitude == 0d) {
            return 0d;
        }
        return dot / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }

    private List<Double> toDoubleList(double[] vector) {
        if (vector == null || vector.length == 0) {
            return Collections.emptyList();
        }
        return new java.util.ArrayList<>(vector.length) {
            private static final long serialVersionUID = 1L;
            {
                for (double value : vector) {
                    add(value);
                }
            }
        };
    }

    private String resolveEmbeddingsEndpoint() {
        String base = openAIProperties.getBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = "https://api.openai.com/v1";
        }
        if (base.endsWith("/")) {
            return base + "embeddings";
        }
        return base + "/embeddings";
    }
}
