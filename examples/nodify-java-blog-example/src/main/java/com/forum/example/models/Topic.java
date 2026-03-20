package com.forum.example.models;

import java.time.Instant;
import java.util.List;

/**
 * Topic model representing a forum discussion thread
 */
public record Topic(
        String id,
        String title,
        String content,
        String author,
        Instant createdAt,
        Instant lastModified,
        int messageCount,
        List<Message> messages
) {
    public Topic withId(String id) {
        return new Topic(id, title, content, author, createdAt, lastModified, messageCount, messages);
    }

    public Topic withMessageCount(int count) {
        return new Topic(id, title, content, author, createdAt, lastModified, count, messages);
    }
}