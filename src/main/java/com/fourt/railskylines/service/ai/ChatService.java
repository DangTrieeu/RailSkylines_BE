package com.fourt.railskylines.service.ai;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fourt.railskylines.config.ChatbotProperties;
import com.fourt.railskylines.config.OpenAIProperties;
import com.fourt.railskylines.domain.Article;

import com.fourt.railskylines.service.ai.SemanticRouter.Route;
import com.fourt.railskylines.service.ai.dto.ChatMessagePayload;
import com.fourt.railskylines.service.ai.dto.ChatRequestPayload;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

@Service
public class ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
    private static final okhttp3.MediaType JSON_MEDIA_TYPE = okhttp3.MediaType.get("application/json");
    private static final int HISTORY_WINDOW = 12;

    private final OpenAIProperties openAIProperties;
    private final ChatbotProperties chatbotProperties;
    private final SemanticRouter semanticRouter;
    private final VectorSearchService vectorSearchService;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public ChatService(
            OpenAIProperties openAIProperties,
            ChatbotProperties chatbotProperties,
            SemanticRouter semanticRouter,
            VectorSearchService vectorSearchService,
            ObjectMapper objectMapper) {
        this.openAIProperties = openAIProperties;
        this.chatbotProperties = chatbotProperties;
        this.semanticRouter = semanticRouter;
        this.vectorSearchService = vectorSearchService;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(15))
                .callTimeout(Duration.ofMinutes(3))
                .readTimeout(Duration.ofMinutes(3))
                .build();
    }

    public SseEmitter streamChat(ChatRequestPayload request) {
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(5));
        CompletableFuture.runAsync(() -> handleChat(request, emitter));
        return emitter;
    }

    private void handleChat(ChatRequestPayload request, SseEmitter emitter) {
        try {
            List<ChatMessagePayload> history = request.messages();
            if (history.isEmpty()) {
                emitError(emitter, "No chat messages provided.");
                return;
            }
            if (!StringUtils.hasText(openAIProperties.getApiKey())) {
                emitError(emitter, "OpenAI API key is not configured.");
                return;
            }

            Route route = semanticRouter.resolveRoute(history);
            List<RelevantArticle> relevantArticles = retrieveRelevantArticles(route, history);
            sendMetadata(emitter, route, relevantArticles);

            List<Map<String, String>> openAiMessages = buildOpenAiMessages(history, route, relevantArticles);
            streamFromOpenAi(emitter, openAiMessages);
        } catch (IOException ioException) {
            if (isClientAbort(ioException)) {
                LOGGER.debug("Chat stream interrupted: {}", ioException.getMessage());
            } else {
                LOGGER.error("Streaming chat failed", ioException);
                emitError(emitter, "Chat service error: " + ioException.getMessage());
            }
        } catch (Exception exception) {
            LOGGER.error("Streaming chat failed", exception);
            emitError(emitter, "Chat service error: " + exception.getMessage());
        }
    }

    private void streamFromOpenAi(SseEmitter emitter, List<Map<String, String>> messages) throws IOException {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", openAIProperties.getChat().getModel());
        payload.put("stream", true);
        payload.put("temperature", 0.3);
        payload.put("max_tokens", openAIProperties.getChat().getMaxOutputTokens());
        payload.set("messages", objectMapper.valueToTree(messages));

        Request httpRequest = new Request.Builder()
                .url(openAIProperties.getBaseUrl() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + openAIProperties.getApiKey())
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .post(RequestBody.create(payload.toString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("OpenAI request failed: " + response.code() + " " + errorBody);
            }
            if (response.body() == null) {
                throw new IOException("OpenAI response body was empty.");
            }
            BufferedSource source = response.body().source();
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring("data:".length()).trim();
                if ("[DONE]".equals(data)) {
                    sendDone(emitter);
                    return;
                }
                emitChunk(emitter, data);
            }
            sendDone(emitter);
        }
    }

    private void emitChunk(SseEmitter emitter, String data) {
        try {
            JsonNode root = objectMapper.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray()) {
                return;
            }
            for (JsonNode node : choices) {
                JsonNode delta = node.path("delta");
                if (delta.has("content")) {
                    String deltaText = delta.path("content").asText();
                    if (deltaText != null && !deltaText.isEmpty()) {
                        ObjectNode chunk = objectMapper.createObjectNode();
                        chunk.put("delta", deltaText);
                        emitter.send(SseEmitter.event()
                                .name("chunk")
                                .data(objectMapper.writeValueAsString(chunk), MediaType.TEXT_PLAIN));
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to process OpenAI chunk: {}", exception.getMessage());
        }
    }

    private void sendMetadata(SseEmitter emitter, Route route, List<RelevantArticle> articles) {
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("route", route.wireValue());
        ArrayNode sources = metadata.putArray("sources");
        for (RelevantArticle article : articles) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("articleId", article.article().getArticleId());
            node.put("title", article.article().getTitle());
            node.put("preview", article.preview());
            node.put("thumbnail", article.article().getThumbnail());
            node.put("score", article.score());
            sources.add(node);
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("metadata")
                    .data(objectMapper.writeValueAsString(metadata), MediaType.TEXT_PLAIN));
        } catch (IOException exception) {
            LOGGER.warn("Failed to send metadata event: {}", exception.getMessage());
        }
    }

    /**
     * Retrieve relevant articles using vector search, similar to Python version
     */
    private List<RelevantArticle> retrieveRelevantArticles(Route route, List<ChatMessagePayload> history) {
        if (route != Route.ARTICLE_QA) {
            return List.of();
        }

        Optional<ChatMessagePayload> latestUser = history.stream()
                .filter(ChatMessagePayload::isUser)
                .reduce((first, second) -> second);
        if (latestUser.isEmpty()) {
            return List.of();
        }

        String query = latestUser.get().content();
        int maxSources = chatbotProperties.getEmbedding().getMaxSources();
        double minScore = -1.0; // Very low threshold to catch mock embeddings

        // Use VectorSearchService for better search logic
        List<VectorSearchService.VectorSearchResult> searchResults = vectorSearchService.vectorSearch(query, maxSources,
                minScore);

        // Convert to RelevantArticle format
        return searchResults.stream()
                .map(result -> new RelevantArticle(
                        result.article(),
                        result.score(),
                        result.preview()))
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> buildOpenAiMessages(List<ChatMessagePayload> history, Route route,
            List<RelevantArticle> articles) {
        List<Map<String, String>> payload = new ArrayList<>();

        // Add system persona prompt
        payload.add(Map.of(
                "role", "system",
                "content", buildPersonaPrompt(route)));

        // For article QA, build a complete context-aware prompt instead of separate
        // system messages
        if (!articles.isEmpty() && route == Route.ARTICLE_QA) {
            // Get the latest user message for context building
            Optional<ChatMessagePayload> latestUser = history.stream()
                    .filter(ChatMessagePayload::isUser)
                    .reduce((first, second) -> second);

            if (latestUser.isPresent()) {
                String completePrompt = buildCompletePrompt(latestUser.get().content(), articles);
                payload.add(Map.of(
                        "role", "system",
                        "content", completePrompt));
            }
        }

        // Add conversation history (trimmed)
        List<ChatMessagePayload> trimmed = history.size() > HISTORY_WINDOW
                ? history.subList(history.size() - HISTORY_WINDOW, history.size())
                : history;

        for (ChatMessagePayload message : trimmed) {
            payload.add(Map.of(
                    "role", normalizeRole(message.role()),
                    "content", message.content()));
        }

        return payload;
    }

    private String buildPersonaPrompt(Route route) {
        return switch (route) {
            case ARTICLE_QA ->
                """
                        You are RailSkylines Copilot. Use the provided article knowledge base to craft precise, well-structured responses in Vietnamese when possible. Reference relevant articles naturally and mention if you include an image thumbnail. If you do not have enough context, ask for clarification instead of inventing details.
                        """;
            case SUPPORT ->
                """
                        You are RailSkylines Copilot helping travellers with bookings, schedules, and support questions. Provide step-by-step guidance and reference official procedures when necessary. Be concise and proactive in Vietnamese unless the user writes in another language.
                        """;
            default ->
                """
                        You are RailSkylines Copilot. Engage in friendly, concise conversation while remaining helpful. When the user switches topics, follow politely and offer assistance related to RailSkylines when appropriate.
                        """;
        };
    }

    /**
     * Build knowledge prompt similar to build_prompt() in Python version
     * Creates a structured context from search results
     */
    private String buildKnowledgePrompt(List<RelevantArticle> articles) {
        if (articles.isEmpty()) {
            return "No relevant articles found in the knowledge base.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Dựa trên thông tin sau từ cơ sở dữ liệu RailSkylines:\\n\\n");

        for (int i = 0; i < articles.size(); i++) {
            RelevantArticle article = articles.get(i);
            String title = Optional.ofNullable(article.article().getTitle()).orElse("Không có tiêu đề");
            String content = Optional.ofNullable(article.article().getContent()).orElse("Không có nội dung");
            String thumbnail = article.article().getThumbnail();

            context.append(String.format("#### %d. **%s**\\n", i + 1, title));
            context.append(String.format("- #### **Thông tin**: %s\\n", content));

            // Add thumbnail/image info if available
            if (StringUtils.hasText(thumbnail)) {
                context.append(String.format("- **Hình ảnh**: %s\\n", thumbnail));
            }

            // Add relevance score for debugging (optional)
            if (LOGGER.isDebugEnabled()) {
                context.append(String.format("- **Độ liên quan**: %.2f\\n", article.score()));
            }

            context.append("\\n");
        }

        context.append("\\nHãy trả lời thân thiện, rõ ràng dựa trên thông tin trên. ");
        context.append("Nếu có hình ảnh, hãy đề cập để người dùng có thể xem.");

        return context.toString();
    }

    /**
     * Build complete prompt for user query with context, similar to Python version
     */
    private String buildCompletePrompt(String userQuery, List<RelevantArticle> articles) {
        String context = buildKnowledgePrompt(articles);

        return String.format(
                "Bạn là chuyên gia tư vấn của **RailSkylines**. " +
                        "**Khách hỏi:** _%s_ " +
                        "%s",
                userQuery,
                context);
    }

    private void sendDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data(""));
        } catch (IOException exception) {
            LOGGER.debug("Failed to send done event: {}", exception.getMessage());
        } finally {
            emitter.complete();
        }
    }

    private void emitError(SseEmitter emitter, String message) {
        try {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("message", message);
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(objectMapper.writeValueAsString(error), MediaType.TEXT_PLAIN));
        } catch (IOException exception) {
            LOGGER.warn("Unable to send error event: {}", exception.getMessage());
        } finally {
            emitter.complete();
        }
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return "user";
        }
        return switch (role.toLowerCase(Locale.ROOT)) {
            case "assistant" -> "assistant";
            case "system" -> "system";
            default -> "user";
        };
    }

    private boolean isClientAbort(IOException exception) {
        String message = exception.getMessage();
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.ROOT);
        return normalized.contains("canceled")
                || normalized.contains("cancelled")
                || normalized.contains("socket closed")
                || normalized.contains("stream was reset");
    }

    private record RelevantArticle(Article article, double score, String preview) {
    }
}
