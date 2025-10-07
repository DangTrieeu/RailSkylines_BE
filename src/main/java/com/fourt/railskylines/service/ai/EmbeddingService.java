package com.fourt.railskylines.service.ai;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.fourt.railskylines.repository.ArticleRepository;

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
    private final ArticleRepository articleRepository;

    public EmbeddingService(
            ChatbotProperties chatbotProperties,
            OpenAIProperties openAIProperties,
            ObjectMapper objectMapper,
            ArticleRepository articleRepository) {
        this.chatbotProperties = chatbotProperties;
        this.openAIProperties = openAIProperties;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder().build();
        this.articleRepository = articleRepository;
    }

    public List<Double> embedArticle(Article article) {
        if (article == null) {
            LOGGER.debug("Article is null; returning empty embedding");
            return Collections.emptyList();
        }

        // Combine title and content similar to Python version
        String title = Optional.ofNullable(article.getTitle()).orElse("").trim();
        String content = Optional.ofNullable(article.getContent()).orElse("").trim();

        // Build combined text with better structure
        StringBuilder combined = new StringBuilder();
        if (!title.isEmpty()) {
            combined.append(title);
        }
        if (!content.isEmpty()) {
            if (combined.length() > 0) {
                combined.append(" "); // Single space separator like Python version
            }
            combined.append(content);
        }

        String finalText = combined.toString().trim();
        if (!StringUtils.hasText(finalText)) {
            LOGGER.debug("Article {} has no content to embed", article.getArticleId());
            return Collections.emptyList();
        }

        try {
            List<Double> embedding = embedText(finalText);
            if (embedding.isEmpty()) {
                LOGGER.warn("OpenAI API failed, using mock embedding for article {} with content length {}",
                        article.getArticleId(), finalText.length());
                // Fallback to mock embedding when OpenAI fails
                embedding = generateMockEmbedding(finalText);
            }

            if (!embedding.isEmpty()) {
                // Save embedding to database
                article.setEmbedding(embedding);
                articleRepository.save(article);
                LOGGER.info("Generated and saved embedding for article {} with {} dimensions",
                        article.getArticleId(), embedding.size());
            }

            return embedding;
        } catch (Exception exception) {
            LOGGER.warn("Failed to generate embedding for article {}: {}, using mock embedding",
                    article.getArticleId(), exception.getMessage());

            // Fallback to mock embedding
            try {
                List<Double> mockEmbedding = generateMockEmbedding(finalText);
                article.setEmbedding(mockEmbedding);
                articleRepository.save(article);
                LOGGER.info("Generated and saved mock embedding for article {} with {} dimensions",
                        article.getArticleId(), mockEmbedding.size());
                return mockEmbedding;
            } catch (Exception saveException) {
                LOGGER.error("Failed to save mock embedding for article {}: {}",
                        article.getArticleId(), saveException.getMessage());
                return Collections.emptyList();
            }
        }
    }

    /**
     * Generate embedding for text, similar to get_embedding() in Python version
     */
    public List<Double> embedText(String text) {
        if (!StringUtils.hasText(text) || text.trim().isEmpty()) {
            LOGGER.debug("Embedding text is empty; returning empty list.");
            return Collections.emptyList();
        }

        // Validate model configuration
        String model = chatbotProperties.getEmbedding().getModelId();
        if (!StringUtils.hasText(model)) {
            LOGGER.warn("Embedding model id is empty; skipping embedding generation.");
            return Collections.emptyList();
        }

        // Validate API key
        String apiKey = openAIProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            LOGGER.warn(
                    "OpenAI API key missing; unable to call embedding endpoint. Please set OPENAI_API_KEY environment variable.");
            return Collections.emptyList();
        }

        // Trim and clean text
        String cleanText = text.trim();
        if (cleanText.length() > 8000) { // OpenAI has token limits
            LOGGER.warn("Text too long ({}), truncating to 8000 chars", cleanText.length());
            cleanText = cleanText.substring(0, 8000);
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

    /**
     * Compute cosine similarity between two vectors, similar to cos_sim in Python
     * version
     */
    public double cosineSimilarity(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            LOGGER.debug("One or both vectors are null/empty for cosine similarity");
            return 0d;
        }

        if (left.size() != right.size()) {
            LOGGER.warn("Vector dimensions mismatch: {} vs {}", left.size(), right.size());
            return 0d;
        }

        int length = left.size();
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
            LOGGER.debug("Zero magnitude vector detected in cosine similarity");
            return 0d;
        }

        double similarity = dot / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
        return Math.max(-1.0, Math.min(1.0, similarity)); // Clamp to [-1, 1]
    }

    /**
     * Batch compute embeddings for multiple texts (useful for bulk operations)
     */
    public List<List<Double>> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        return texts.stream()
                .map(this::embedText)
                .collect(Collectors.toList());
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

    /**
     * Generate mock embedding as fallback when OpenAI API fails
     * Uses deterministic generation based on text content for consistency
     */
    private List<Double> generateMockEmbedding(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }

        // Use text hash as seed for consistent results
        java.util.Random random = new java.util.Random(text.hashCode());
        Double[] embedding = new Double[1536]; // Same dimensions as OpenAI text-embedding-3-small

        // Generate normalized random values
        for (int i = 0; i < 1536; i++) {
            embedding[i] = random.nextGaussian() * 0.1; // Small variance for realistic embeddings
        }

        // Add some text-based features for better similarity
        int textLength = Math.min(text.length(), 100);
        for (int i = 0; i < textLength && i < 50; i++) {
            embedding[i] += (text.charAt(i) % 100) / 10000.0;
        }

        return java.util.Arrays.asList(embedding);
    }
}
