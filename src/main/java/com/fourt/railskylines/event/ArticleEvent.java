package com.fourt.railskylines.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleEvent {

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    private EventType eventType;
    private Long articleId;
    private String title;
    private String content;
    private String thumbnail;
    private Long userId;
    private String userName;
    private Instant timestamp;

    // Constructor for easy creation
    public ArticleEvent(EventType eventType, Long articleId, String title, String content,
                       String thumbnail, Long userId, String userName) {
        this.eventType = eventType;
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.thumbnail = thumbnail;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = Instant.now();
    }
}
