package com.fourt.railskylines.service.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.fourt.railskylines.domain.Article;

/**
 * Mock embedding service for testing/development when OpenAI API is not
 * available
 * Generates deterministic fake embeddings based on content hash
 */
@Service
@ConditionalOnProperty(name = "chatbot.mock-embeddings", havingValue = "true", matchIfMissing = false)
public class MockEmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockEmbeddingService.class);
    private static final int EMBEDDING_DIMENSION = 1536; // OpenAI text-embedding-ada-002 dimension

    /**
     * Generate deterministic mock embedding for article
     */
    public List<Double> generateMockEmbedding(Article article) {
        if (article == null) {
            return Collections.emptyList();
        }

        String content = "";
        if (article.getTitle() != null) {
            content += article.getTitle();
        }
        if (article.getContent() != null) {
            content += " " + article.getContent();
        }

        if (content.trim().isEmpty()) {
            LOGGER.debug("Article {} has no content for mock embedding", article.getArticleId());
            return Collections.emptyList();
        }

        // Generate deterministic embedding based on content hash
        int seed = content.hashCode();
        Random random = new Random(seed);

        double[] vector = new double[EMBEDDING_DIMENSION];
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            vector[i] = random.nextGaussian(); // Normal distribution
        }

        // Normalize vector to unit length (like real embeddings)
        double magnitude = Math.sqrt(Arrays.stream(vector).map(x -> x * x).sum());
        if (magnitude > 0) {
            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                vector[i] /= magnitude;
            }
        }

        List<Double> embedding = Arrays.stream(vector).boxed().collect(Collectors.toList());

        LOGGER.debug("Generated mock embedding for article {} with {} dimensions",
                article.getArticleId(), embedding.size());

        return embedding;
    }

    /**
     * Generate mock embedding for text
     */
    public List<Double> generateMockEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Similar logic as article but for raw text
        int seed = text.hashCode();
        Random random = new Random(seed);

        double[] vector = new double[EMBEDDING_DIMENSION];
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            vector[i] = random.nextGaussian();
        }

        // Normalize
        double magnitude = Math.sqrt(Arrays.stream(vector).map(x -> x * x).sum());
        if (magnitude > 0) {
            for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
                vector[i] /= magnitude;
            }
        }

        return Arrays.stream(vector).boxed().collect(Collectors.toList());
    }
}