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

    // Small talk samples - chỉ những câu chào hỏi thông thường
    private static final List<String> SMALL_TALK_SAMPLES = List.of(
            // Tiếng Việt
            "Xin chào",
            "Chào bạn",
            "Hi",
            "Hello",
            "Bạn khỏe không",
            "Bạn có khỏe không",
            "How are you",
            "Cảm ơn",
            "Cảm ơn bạn",
            "Thank you",
            "Thanks",
            "Tạm biệt",
            "Bye",
            "Goodbye",
            "See you",
            "Hẹn gặp lại",
            "Chúc ngủ ngon",
            "Good night",
            "Chào buổi sáng",
            "Good morning",
            "Chào buổi chiều",
            "Good afternoon",
            "Bạn tên gì",
            "What's your name",
            "Nice to meet you",
            "Rất vui được gặp bạn",
            "Hôm nay thế nào",
            "How's your day",
            "Thời tiết hôm nay thế nào",
            "How's the weather",
            "Bạn là ai",
            "Who are you",
            "Bạn có thể giúp gì",
            "Can you help me",
            "Bạn làm gì được",
            "What can you do");

    private final EmbeddingService embeddingService;

    public SemanticRouter(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * Simple routing: nếu là small talk thì return SMALL_TALK, còn lại là
     * ARTICLE_QA
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

        // Thử semantic similarity trước
        List<Double> queryEmbedding = embeddingService.embedText(query);
        if (!queryEmbedding.isEmpty()) {
            double smallTalkScore = calculateRouteScore(queryEmbedding, SMALL_TALK_SAMPLES);
            LOGGER.debug("Query: '{}' -> Small talk score: {:.3f}", query, smallTalkScore);

            // Threshold để xác định small talk
            if (smallTalkScore > 0.3) {
                LOGGER.debug("Query: '{}' -> Route: SMALL_TALK (score: {:.3f})", query, smallTalkScore);
                return Route.SMALL_TALK;
            }
        }

        // Fallback keyword matching
        if (isSmallTalkKeyword(query)) {
            LOGGER.debug("Query: '{}' -> Route: SMALL_TALK (keyword matching)", query);
            return Route.SMALL_TALK;
        }

        // Mặc định là ARTICLE_QA cho tất cả các câu hỏi khác
        LOGGER.debug("Query: '{}' -> Route: ARTICLE_QA (default)", query);
        return Route.ARTICLE_QA;
    }

    /**
     * Calculate average similarity score between query and samples
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
     * Fallback keyword matching for small talk
     */
    private boolean isSmallTalkKeyword(String query) {
        String normalized = query.toLowerCase(Locale.ROOT).trim();

        // Exact matches hoặc very simple patterns
        return normalized.equals("hi") ||
                normalized.equals("hello") ||
                normalized.equals("xin chào") ||
                normalized.equals("chào") ||
                normalized.equals("chào bạn") ||
                normalized.equals("cảm ơn") ||
                normalized.equals("thank you") ||
                normalized.equals("thanks") ||
                normalized.equals("bye") ||
                normalized.equals("goodbye") ||
                normalized.equals("tạm biệt") ||
                normalized.startsWith("bạn khỏe") ||
                normalized.startsWith("how are you") ||
                normalized.startsWith("bạn tên") ||
                normalized.startsWith("what's your name");
    }

    public enum Route {
        ARTICLE_QA,
        SMALL_TALK;

        public String wireValue() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}