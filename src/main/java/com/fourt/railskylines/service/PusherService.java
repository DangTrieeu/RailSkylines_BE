package com.fourt.railskylines.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.event.ArticleEvent;
import com.pusher.rest.Pusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PusherService {

    private static final Logger log = LoggerFactory.getLogger(PusherService.class);
    private static final String CHANNEL_NAME = "articles-channel";
    private static final String EVENT_NAME = "article-event";

    @Autowired
    private Pusher pusher;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Trigger article created event
     */
    public void triggerArticleCreated(Article article) {
        ArticleEvent event = new ArticleEvent(
                ArticleEvent.EventType.CREATED,
                article.getArticleId(),
                article.getTitle(),
                article.getContent(),
                article.getThumbnail(),
                article.getUser() != null ? article.getUser().getUserId() : null,
                article.getUser() != null ? article.getUser().getFullName() : null
        );
        triggerEvent(event);
    }

    /**
     * Trigger article updated event
     */
    public void triggerArticleUpdated(Article article) {
        ArticleEvent event = new ArticleEvent(
                ArticleEvent.EventType.UPDATED,
                article.getArticleId(),
                article.getTitle(),
                article.getContent(),
                article.getThumbnail(),
                article.getUser() != null ? article.getUser().getUserId() : null,
                article.getUser() != null ? article.getUser().getFullName() : null
        );
        triggerEvent(event);
    }

    /**
     * Trigger article deleted event
     */
    public void triggerArticleDeleted(Long articleId) {
        ArticleEvent event = new ArticleEvent(
                ArticleEvent.EventType.DELETED,
                articleId,
                null,
                null,
                null,
                null,
                null
        );
        triggerEvent(event);
    }

    /**
     * Send event to Pusher
     */
    private void triggerEvent(ArticleEvent event) {
        try {
            pusher.trigger(CHANNEL_NAME, EVENT_NAME, objectMapper.writeValueAsString(event));
            log.info("Pusher event triggered: {} for article ID: {}", event.getEventType(), event.getArticleId());
        } catch (Exception e) {
            log.error("Failed to trigger Pusher event: {}", e.getMessage(), e);
        }
    }

    /**
     * Get channel name for FE to subscribe
     */
    public static String getChannelName() {
        return CHANNEL_NAME;
    }

    /**
     * Get event name for FE to listen
     */
    public static String getEventName() {
        return EVENT_NAME;
    }
}
