package com.blog.example.services;

import com.blog.example.models.Author;
import com.blog.example.models.BlogPost;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.ContentTypeEnum;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.ContentNode;
import io.github.azirarm.content.lib.models.Translation;
import io.github.azirarm.content.lib.models.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlogService {
    private final ReactiveNodifyClient client;
    private final String blogSiteCode;
    private final ObjectMapper mapper = new ObjectMapper();

    public BlogService(ReactiveNodifyClient client, String blogSiteCode) {
        this.client = client;
        this.blogSiteCode = blogSiteCode;
    }

    public Mono<BlogPost> createBlogPost(BlogPost post) {
        System.out.println("\n📝 Creating post: " + post.title());

        ContentNode node = new ContentNode();
        node.setParentCode(blogSiteCode);
        node.setType(ContentTypeEnum.HTML);
        node.setTitle(post.title());
        node.setCode(generatePostCode(post.title()));
        node.setSlug(post.slug());
        node.setLanguage(post.language() != null ? post.language() : "en");
        node.setStatus(StatusEnum.SNAPSHOT);
        node.setEnvironmentCode("production");
        node.setDescription(post.excerpt());
        node.setContent(post.content());

        if (post.author() != null) {
            List<Value> values = new ArrayList<>();
            values.add(createValue("AUTHOR_NAME", post.author().name()));
            values.add(createValue("AUTHOR_EMAIL", post.author().email()));
            values.add(createValue("AUTHOR_BIO", post.author().bio()));
            node.setValues(values);
        }

        return client.saveContentNode(node)
                .doOnNext(saved -> System.out.println("  ✅ Node saved with code: " + saved.getCode()))
                .map(savedNode -> post.withId(savedNode.getCode()))
                .doOnError(e -> System.err.println("  ❌ Error saving node: " + e.getMessage()));
    }

    public Flux<BlogPost> findAllPublishedPosts() {
        return client.findAllContentNodes()
                .cast(Object.class)
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .filter(node -> blogSiteCode.equals(node.getParentCode()))
                .map(this::toBlogPost)
                .filter(Objects::nonNull)
                .filter(BlogPost::published);
    }

    public Mono<BlogPost> findBlogPostByCode(String code) {
        return client.findContentNodeByCodeAndStatus(code, "PUBLISHED")
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .map(this::toBlogPost);
    }

    public Mono<BlogPost> publishBlogPost(String postCode) {
        System.out.println("\n🚀 Publishing: " + postCode);
        return client.publishContentNode(postCode, true)
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .map(this::toBlogPost)
                .map(post -> post.withPublished(true))
                .doOnError(e -> System.err.println("  ❌ Error publishing: " + e.getMessage()));
    }

    public Mono<Boolean> deleteBlogPost(String code) {
        System.out.println("\n🗑️ Deleting: " + code);
        return client.deleteContentNode(code)
                .map(result -> {
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                    return true;
                })
                .doOnError(e -> System.err.println("  ❌ Error deleting: " + e.getMessage()))
                .onErrorReturn(false);
    }

    public Mono<BlogPost> addTranslation(String postCode, String language,
                                         String translatedTitle, String translatedContent) {
        System.out.println("\n🌍 Adding " + language + " translation to: " + postCode);

        return client.findContentNodeByCodeAndStatus(postCode, "SNAPSHOT")
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .flatMap(node -> {
                    List<Translation> translations = node.getTranslations();
                    if (translations == null) {
                        translations = new ArrayList<>();
                    }

                    translations.removeIf(t ->
                            t.getLanguage().equals(language) &&
                                    ("TITLE".equals(t.getKey()) || "CONTENT".equals(t.getKey()))
                    );

                    translations.add(createTranslation("TITLE", language, translatedTitle));
                    translations.add(createTranslation("CONTENT", language, translatedContent));

                    node.setTranslations(translations);
                    return client.saveContentNode(node);
                })
                .map(this::toBlogPost)
                .doOnError(e -> System.err.println("  ❌ Error adding translation: " + e.getMessage()));
    }

    private ContentNode toContentNode(Object obj) {
        if (obj == null) return null;
        if (obj instanceof ContentNode node) {
            return node;
        }
        if (obj instanceof Map) {
            return mapper.convertValue(obj, ContentNode.class);
        }
        System.err.println("Unexpected type: " + obj.getClass().getName());
        return null;
    }

    private BlogPost toBlogPost(ContentNode node) {
        if (node == null) return null;

        Author author = extractAuthor(node.getValues());

        if (node.getTranslations() != null && !node.getTranslations().isEmpty()) {
            System.out.println("      🌍 Translations found:");
            for (Translation t : node.getTranslations()) {
                System.out.println("        - [" + t.getLanguage() + "] " +
                        t.getKey() + ": " + t.getValue());
            }
        }

        return new BlogPost(
                node.getCode(),
                node.getTitle(),
                node.getContent(),
                node.getDescription() != null ? node.getDescription() : "",
                author,
                List.of(),
                node.getSlug(),
                node.getLanguage(),
                Instant.ofEpochMilli(node.getCreationDate()),
                Instant.ofEpochMilli(node.getModificationDate()),
                StatusEnum.PUBLISHED.equals(node.getStatus())
        );
    }

    private Author extractAuthor(List<Value> values) {
        String name = "Unknown Author";
        String email = "unknown@example.com";
        String bio = "No bio available";

        if (values != null) {
            for (Value value : values) {
                if ("AUTHOR_NAME".equals(value.getKey())) {
                    name = value.getValue();
                } else if ("AUTHOR_EMAIL".equals(value.getKey())) {
                    email = value.getValue();
                } else if ("AUTHOR_BIO".equals(value.getKey())) {
                    bio = value.getValue();
                }
            }
        }
        return new Author(name, email, bio, null);
    }

    private Value createValue(String key, String value) {
        Value v = new Value();
        v.setKey(key);
        v.setValue(value);
        return v;
    }

    private Translation createTranslation(String key, String language, String value) {
        Translation t = new Translation();
        t.setKey(key);
        t.setLanguage(language);
        t.setValue(value);
        return t;
    }

    private String generatePostCode(String title) {
        String base = title.toUpperCase()
                .replaceAll("[^a-zA-Z0-9]", "-")
                .replaceAll("-+", "-");
        if (base.length() > 20) base = base.substring(0, 20);
        if (base.endsWith("-")) base = base.substring(0, base.length() - 1);
        return "POST-" + base + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}