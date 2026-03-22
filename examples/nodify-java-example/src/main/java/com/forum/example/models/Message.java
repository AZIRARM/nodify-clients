package com.forum.example.models;

import java.time.Instant;

/**
 * Message model for forum replies
 */
public record Message(
        String id,
        String topicId,
        String author,
        String content,
        Instant createdAt
) {
    public Message withId(String id) {
        return new Message(id, topicId, author, content, createdAt);
    }
}