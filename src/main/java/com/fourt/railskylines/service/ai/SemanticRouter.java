package com.fourt.railskylines.service.ai;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fourt.railskylines.service.ai.dto.ChatMessagePayload;

@Component
public class SemanticRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticRouter.class);

    // Define sample utterances for each route, similar to Python version
    private static final List<String> ARTICLE_QA_SAMPLES = List.of(
            "Cho tôi xem tin tức về tàu hỏa",
            "Có chương trình khuyến mãi gì không",
            "Bài viết về lịch trình tàu",
            "Hình ảnh của ga tàu",
            "Tin tức mới nhất về RailSkylines",
            "Thông tin về các tuyến đường mới",
            "What are the latest news",
            "Show me articles about trains",
            "Any promotions available");

    private static final List<String> SUPPORT_SAMPLES = List.of(
            "Tôi muốn đặt vé tàu",
            "Làm thế nào để hủy vé",
            "Lịch trình tàu từ Hà Nội đi Sài Gòn",
            "Giá vé tàu bao nhiêu",
            "Tôi cần hỗ trợ đặt vé",
            "Refund ticket process",
            "How to book train tickets",
            "Train schedule information",
            "Help me with booking");

    private static final List<String> CHITCHAT_SAMPLES = List.of(
            "Xin chào",
            "Bạn khỏe không",
            "Cảm ơn bạn",
            "Tạm biệt",
            "Hello there",
            "How are you",
            "Thank you",
            "Goodbye",
            "Nice to meet you");

    private final EmbeddingService embeddingService;

    public SemanticRouter(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * Resolve route using semantic similarity, similar to Python version
     */
    public Route resolveRoute(List<ChatMessagePayload> messages) {
        Optional<ChatMessagePayload> latestUser = messages.stream()
                .filter(ChatMessagePayload::isUser)
                .reduce((first, second) -> second);
        if (latestUser.isEmpty()) {
            return Route.SMALL_TALK;
        }

        String query = latestUser.get().content();
        if (!StringUtils.hasText(query)) {
            return Route.SMALL_TALK;
        }

        // Get embedding for user query
        List<Double> queryEmbedding = embeddingService.embedText(query);
        if (queryEmbedding.isEmpty()) {
            LOGGER.debug("Failed to get embedding for query, falling back to keyword matching");
            return fallbackKeywordMatching(query);
        }

        // Calculate similarity with each route's sample utterances
        double bestScore = -1.0;
        Route bestRoute = Route.SMALL_TALK;

        // Check ARTICLE_QA route
        double articleScore = calculateRouteScore(queryEmbedding, ARTICLE_QA_SAMPLES);
        if (articleScore > bestScore) {
            bestScore = articleScore;
            bestRoute = Route.ARTICLE_QA;
        }

        // Check SUPPORT route
        double supportScore = calculateRouteScore(queryEmbedding, SUPPORT_SAMPLES);
        if (supportScore > bestScore) {
            bestScore = supportScore;
            bestRoute = Route.SUPPORT;
        }

        // Check CHITCHAT route
        double chitchatScore = calculateRouteScore(queryEmbedding, CHITCHAT_SAMPLES);
        if (chitchatScore > bestScore) {
            bestScore = chitchatScore;
            bestRoute = Route.SMALL_TALK;
        }

        LOGGER.debug("Query: '{}' -> Route: {} (score: {:.3f})", query, bestRoute, bestScore);
        return bestRoute;
    }

    /**
     * Calculate average similarity score between query and route samples
     */
    private double calculateRouteScore(List<Double> queryEmbedding, List<String> samples) {
        if (samples.isEmpty()) {
            return 0.0;
        }

        double totalScore = 0.0;
        int validSamples = 0;

        for (String sample : samples) {
            List<Double> sampleEmbedding = embeddingService.embedText(sample);
            if (!sampleEmbedding.isEmpty()) {
                double similarity = embeddingService.cosineSimilarity(queryEmbedding, sampleEmbedding);
                totalScore += similarity;
                validSamples++;
            }
        }

        return validSamples > 0 ? totalScore / validSamples : 0.0;
    }

    /**
     * Fallback keyword matching when embeddings fail
     */
    private Route fallbackKeywordMatching(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);

        // Simple keyword matching as fallback
        if (normalized.contains("tin tức") || normalized.contains("bài viết") ||
                normalized.contains("news") || normalized.contains("article") ||
                normalized.contains("khuyến mãi") || normalized.contains("promotion") ||
                normalized.contains("camels") || normalized.contains("lạc đà") ||
                normalized.contains("animals") || normalized.contains("động vật") ||
                normalized.contains("about") || normalized.contains("tell me")) {
            return Route.ARTICLE_QA;
        }

        if (normalized.contains("đặt vé") || normalized.contains("booking") ||
                normalized.contains("lịch trình") || normalized.contains("schedule") ||
                normalized.contains("hỗ trợ") || normalized.contains("support")) {
            return Route.SUPPORT;
        }

        return Route.SMALL_TALK;
    }

    public enum Route {
        ARTICLE_QA,
        SUPPORT,
        SMALL_TALK;

        public String wireValue() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
