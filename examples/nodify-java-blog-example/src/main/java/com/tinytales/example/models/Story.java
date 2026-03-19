package com.tinytales.example.models;

import io.github.azirarm.content.lib.enums.ContentTypeEnum;
import java.time.Instant;
import java.util.List;

/**
 * Story record representing a children's story
 */
public record Story(
        String id,
        String title,
        String content,
        String excerpt,
        Author author,
        List<String> tags,
        String slug,
        String language,
        ContentTypeEnum type,
        Instant publishedDate,
        Instant lastModified,
        boolean published
) {
    public Story withId(String id) {
        return new Story(
                id, title, content, excerpt, author, tags,
                slug, language, type, publishedDate, lastModified, published
        );
    }

    public Story withPublished(boolean published) {
        return new Story(
                id, title, content, excerpt, author, tags,
                slug, language, type, publishedDate, lastModified, published
        );
    }

    @Override
    public String toString() {
        return String.format("Story{title='%s', author=%s, language=%s}",
                title, author.name(), language);
    }
}