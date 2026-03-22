package com.blog.example.models;

import java.time.Instant;
import java.util.List;

import io.github.azirarm.content.lib.enums.ContentTypeEnum;

/**
 * BlogPost record representing a blog post
 * Using Java 17 record feature for immutable data
 */
public record BlogPost(
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
    public BlogPost withId(String id) {
        return new BlogPost(
                id, title, content, excerpt, author, tags,
                slug, language, type, publishedDate, lastModified, published
        );
    }

    public BlogPost withPublished(boolean published) {
        return new BlogPost(
                id, title, content, excerpt, author, tags,
                slug, language, type, publishedDate, lastModified, published
        );
    }

    @Override
    public String toString() {
        return String.format("BlogPost{title='%s', author=%s, type=%s, published=%s}",
                title, author.name(), type, published);
    }
}