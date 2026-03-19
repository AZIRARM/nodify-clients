package com.tinytales.example.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinytales.example.models.Author;
import com.tinytales.example.models.Story;
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.ContentTypeEnum;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.ContentNode;
import io.github.azirarm.content.lib.models.Node;
import io.github.azirarm.content.lib.models.Translation;
import io.github.azirarm.content.lib.models.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

public class TinyTalesService {
    private final ReactiveNodifyClient client;
    private final String siteCode;
    private final ObjectMapper mapper = new ObjectMapper();

    public TinyTalesService(ReactiveNodifyClient client, String siteCode) {
        this.client = client;
        this.siteCode = siteCode;
    }

    public Mono<Story> createStory(Story story) {
        System.out.println("\n📝 Creating story: " + story.title());

        ContentNode node = new ContentNode();
        node.setParentCode(siteCode);
        node.setType(story.type() != null ? story.type() : ContentTypeEnum.HTML);
        node.setTitle(story.title());
        node.setCode(generateStoryCode(story.title()));
        node.setSlug(story.slug());
        node.setLanguage(story.language() != null ? story.language() : "en");
        node.setStatus(StatusEnum.SNAPSHOT);
        node.setEnvironmentCode("production");
        node.setDescription(story.excerpt());
        node.setContent(story.content());

        if (story.author() != null) {
            List<Value> values = new ArrayList<>();
            values.add(createValue("AUTHOR_NAME", story.author().name()));
            values.add(createValue("AUTHOR_EMAIL", story.author().email()));
            values.add(createValue("AUTHOR_BIO", story.author().bio()));
            node.setValues(values);
        }

        return client.saveContentNode(node)
                .doOnNext(saved -> System.out.println("  ✅ Story saved with code: " + saved.getCode()))
                .map(savedNode -> story.withId(savedNode.getCode()))
                .doOnError(e -> System.err.println("  ❌ Error saving story: " + e.getMessage()));
    }

    public Mono<Story> publishStory(String storyCode) {
        System.out.println("\n🚀 Publishing: " + storyCode);
        return client.publishContentNode(storyCode, true)
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .map(this::toStory)
                .map(story -> story.withPublished(true))
                .doOnError(e -> System.err.println("  ❌ Error publishing: " + e.getMessage()));
    }

    public Mono<Story> addTranslationKey(String storyCode, String language,
                                         String key, String value) {
        System.out.println("\n🌍 Adding translation key " + key + " for " + language);

        return client.findContentNodeByCodeAndStatus(storyCode, "SNAPSHOT")
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .flatMap(node -> {
                    List<Translation> translations = node.getTranslations();
                    if (translations == null) {
                        translations = new ArrayList<>();
                    }
                    translations.removeIf(t ->
                            t.getLanguage().equalsIgnoreCase(language) && key.equals(t.getKey())
                    );
                    translations.add(createTranslation(key, language, value));
                    node.setTranslations(translations);
                    return client.saveContentNode(node);
                })
                .map(this::toStory)
                .doOnError(e -> System.err.println("  ❌ Error adding translation key: " + e.getMessage()));
    }

    private ContentNode toContentNode(Object obj) {
        if (obj == null) return null;
        if (obj instanceof ContentNode node) return node;
        if (obj instanceof Map) return mapper.convertValue(obj, ContentNode.class);
        return null;
    }

    private Story toStory(ContentNode node) {
        if (node == null) return null;
        Author author = extractAuthor(node.getValues());
        ContentTypeEnum type = node.getType() != null ? node.getType() : ContentTypeEnum.HTML;
        return new Story(
                node.getCode(),
                node.getTitle(),
                node.getContent(),
                node.getDescription() != null ? node.getDescription() : "",
                author,
                List.of(),
                node.getSlug(),
                node.getLanguage(),
                type,
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
                if ("AUTHOR_NAME".equals(value.getKey())) name = value.getValue();
                else if ("AUTHOR_EMAIL".equals(value.getKey())) email = value.getValue();
                else if ("AUTHOR_BIO".equals(value.getKey())) bio = value.getValue();
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

    private String generateStoryCode(String title) {
        String base = title.toUpperCase()
                .replaceAll("[^a-zA-Z0-9]", "-")
                .replaceAll("-+", "-");
        if (base.length() > 20) base = base.substring(0, 20);
        if (base.endsWith("-")) base = base.substring(0, base.length() - 1);
        return "STORY-" + base + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Mono<Node> publishNode(String nodeCode) {
        System.out.println("\n🚀 Publishing node: " + nodeCode);
        return client.publishNode(nodeCode);
    }

    public Mono<Story> createStory(Story story, String parentCode) {
        System.out.println("\n📝 Creating story: " + story.title());

        ContentNode node = new ContentNode();
        node.setParentCode(parentCode);  // Utiliser le parent spécifié
        node.setType(story.type() != null ? story.type() : ContentTypeEnum.HTML);
        node.setTitle(story.title());
        node.setCode(generateStoryCode(story.title()));
        node.setSlug(story.slug());
        node.setLanguage(story.language() != null ? story.language() : "en");
        node.setStatus(StatusEnum.SNAPSHOT);
        node.setEnvironmentCode("production");
        node.setDescription(story.excerpt());
        node.setContent(story.content());

        if (story.author() != null) {
            List<Value> values = new ArrayList<>();
            values.add(createValue("AUTHOR_NAME", story.author().name()));
            values.add(createValue("AUTHOR_EMAIL", story.author().email()));
            values.add(createValue("AUTHOR_BIO", story.author().bio()));
            node.setValues(values);
        }

        return client.saveContentNode(node)
                .doOnNext(saved -> System.out.println("  ✅ Story saved with code: " + saved.getCode()))
                .map(savedNode -> story.withId(savedNode.getCode()))
                .doOnError(e -> System.err.println("  ❌ Error saving story: " + e.getMessage()));
    }
}
