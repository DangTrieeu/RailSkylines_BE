package com.fourt.railskylines.service.ai;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fourt.railskylines.service.ai.dto.ChatMessagePayload;

@Component
public class SemanticRouter {

    private static final List<String> ARTICLE_KEYWORDS = List.of(
            "article",
            "tin tức",
            "bài viết",
            "news",
            "post",
            "promotion",
            "khuyến mãi",
            "blog",
            "update",
            "thumnail",
            "thumbnail",
            "hình ảnh",
            "ảnh");

    private static final List<String> SUPPORT_KEYWORDS = List.of(
            "đặt vé",
            "ticket",
            "booking",
            "support",
            "help",
            "refund",
            "hỗ trợ",
            "giải đáp",
            "lịch trình",
            "schedule");

    public Route resolveRoute(List<ChatMessagePayload> messages) {
        Optional<ChatMessagePayload> latestUser = messages.stream()
                .filter(ChatMessagePayload::isUser)
                .reduce((first, second) -> second);
        if (latestUser.isEmpty()) {
            return Route.SMALL_TALK;
        }
        String content = latestUser.get().content();
        if (!StringUtils.hasText(content)) {
            return Route.SMALL_TALK;
        }
        String normalized = content.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, ARTICLE_KEYWORDS)) {
            return Route.ARTICLE_QA;
        }
        if (containsAny(normalized, SUPPORT_KEYWORDS)) {
            return Route.SUPPORT;
        }
        return Route.SMALL_TALK;
    }

    private boolean containsAny(String input, List<String> keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
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
