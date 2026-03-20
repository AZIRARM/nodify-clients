package com.forum.example.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.example.models.Message;
import com.forum.example.models.Topic;
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.ContentTypeEnum;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.ContentNode;
import io.github.azirarm.content.lib.models.Data;
import io.github.azirarm.content.lib.models.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

public class ForumService {

    private final ReactiveNodifyClient client;
    private final String forumSiteCode;
    private final String topicsNodeCode;
    private final ObjectMapper mapper = new ObjectMapper();

    public ForumService(ReactiveNodifyClient client, String forumSiteCode, String topicsNodeCode) {
        this.client = client;
        this.forumSiteCode = forumSiteCode;
        this.topicsNodeCode = topicsNodeCode;
    }

    // ==================== CONVERSION ====================

    @SuppressWarnings("unchecked")
    private ContentNode toContentNode(Object obj) {
        if (obj == null) return null;
        if (obj instanceof ContentNode) return (ContentNode) obj;
        if (!(obj instanceof Map)) return null;

        Map<String, Object> map = (Map<String, Object>) obj;
        ContentNode node = new ContentNode();

        if (map.get("code") != null) node.setCode((String) map.get("code"));
        if (map.get("title") != null) node.setTitle((String) map.get("title"));
        if (map.get("content") != null) node.setContent((String) map.get("content"));
        if (map.get("description") != null) node.setDescription((String) map.get("description"));
        if (map.get("slug") != null) node.setSlug((String) map.get("slug"));
        if (map.get("language") != null) node.setLanguage((String) map.get("language"));
        if (map.get("parentCode") != null) node.setParentCode((String) map.get("parentCode"));

        if (map.get("status") != null) {
            try {
                node.setStatus(StatusEnum.valueOf((String) map.get("status")));
            } catch (IllegalArgumentException e) {
                node.setStatus(StatusEnum.SNAPSHOT);
            }
        }

        if (map.get("type") != null) {
            try {
                node.setType(ContentTypeEnum.valueOf((String) map.get("type")));
            } catch (IllegalArgumentException e) {
                node.setType(ContentTypeEnum.HTML);
            }
        }

        if (map.containsKey("values") && map.get("values") instanceof List) {
            List<Map<String, String>> valueMaps = (List<Map<String, String>>) map.get("values");
            List<Value> values = new ArrayList<>();
            for (Map<String, String> valueMap : valueMaps) {
                Value value = new Value();
                value.setKey(valueMap.get("key"));
                value.setValue(valueMap.get("value"));
                values.add(value);
            }
            node.setValues(values);
        }

        if (map.containsKey("creationDate") && map.get("creationDate") instanceof Number) {
            node.setCreationDate(((Number) map.get("creationDate")).longValue());
        }
        if (map.containsKey("modificationDate") && map.get("modificationDate") instanceof Number) {
            node.setModificationDate(((Number) map.get("modificationDate")).longValue());
        }

        return node;
    }

    // ==================== TOPIC OPERATIONS ====================

    public Mono<Topic> createTopic(String title, String content, String author) {
        System.out.println("\n📝 Creating topic: " + title);

        ContentNode node = new ContentNode();
        node.setParentCode(topicsNodeCode);
        node.setType(ContentTypeEnum.HTML);
        node.setTitle(title);
        node.setCode(generateTopicCode(title));
        node.setSlug(title.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        node.setLanguage("en");
        node.setStatus(StatusEnum.SNAPSHOT);
        node.setEnvironmentCode("production");
        node.setDescription(content);
        node.setContent(content);

        Value authorValue = new Value();
        authorValue.setKey("AUTHOR");
        authorValue.setValue(author);
        node.setValues(List.of(authorValue));

        return client.saveContentNode(node)
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .map(saved -> new Topic(
                        saved.getCode(),
                        saved.getTitle(),
                        saved.getContent(),
                        author,
                        Instant.ofEpochMilli(saved.getCreationDate()),
                        Instant.ofEpochMilli(saved.getModificationDate()),
                        0,
                        new ArrayList<>()
                ))
                .doOnSuccess(t -> System.out.println("  ✅ Topic created: " + t.title()));
    }

    public Flux<Topic> getAllTopics() {
        return client.findAllContentNodes()
                .flatMap(obj -> {
                    try {
                        ContentNode node;
                        if (obj instanceof ContentNode) {
                            node = (ContentNode) obj;
                        } else if (obj instanceof Map) {
                            node = toContentNode((Map<String, Object>) obj);
                        } else {
                            return Flux.empty();
                        }

                        if (!topicsNodeCode.equals(node.getParentCode())) {
                            return Flux.empty();
                        }

                        Topic topic = new Topic(
                                node.getCode(),
                                node.getTitle(),
                                node.getContent(),
                                extractAuthor(node.getValues()),
                                Instant.ofEpochMilli(node.getCreationDate()),
                                Instant.ofEpochMilli(node.getModificationDate()),
                                0,
                                new ArrayList<>()
                        );
                        return Flux.just(topic);
                    } catch (Exception e) {
                        System.err.println("Error converting topic: " + e.getMessage());
                        return Flux.empty();
                    }
                });
    }

    public Flux<Topic> searchTopics(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTopics();
        }

        String lowerKeyword = keyword.toLowerCase();
        return getAllTopics()
                .filter(topic ->
                        topic.title().toLowerCase().contains(lowerKeyword) ||
                                topic.content().toLowerCase().contains(lowerKeyword)
                );
    }

    public Mono<Topic> getTopicWithMessages(String topicCode) {
        return client.findContentNodeByCodeAndStatus(topicCode, "SNAPSHOT")
                .map(this::toContentNode)
                .filter(Objects::nonNull)
                .flatMap(node -> {
                    String author = extractAuthor(node.getValues());
                    return getMessagesForTopic(topicCode)
                            .collectList()
                            .map(messages -> new Topic(
                                    node.getCode(),
                                    node.getTitle(),
                                    node.getContent(),
                                    author,
                                    Instant.ofEpochMilli(node.getCreationDate()),
                                    Instant.ofEpochMilli(node.getModificationDate()),
                                    messages.size(),
                                    messages
                            ));
                });
    }

    // ==================== MESSAGE OPERATIONS ====================

    /**
     * Add a message to a topic using Data
     */
    public Mono<Message> addMessage(String topicCode, String author, String content) {
        System.out.println("\n💬 Adding message to topic: " + topicCode);

        Data data = new Data();
        data.setContentNodeCode(topicCode);
        data.setDataType("message");
        data.setName("message_" + UUID.randomUUID());
        data.setUser(author);
        data.setKey("message");
        data.setValue(content);
        data.setCreationDate(System.currentTimeMillis());
        data.setModificationDate(System.currentTimeMillis());

        return client.saveData(data)
                .map(saved -> new Message(
                        saved.getId().toString(),
                        topicCode,
                        saved.getUser(),
                        saved.getValue(),
                        Instant.ofEpochMilli(saved.getCreationDate())
                ))
                .doOnSuccess(msg -> System.out.println("  ✅ Message added by: " + msg.author()));
    }

    /**
     * Get all messages for a topic
     */
    public Flux<Message> getMessagesForTopic(String topicCode) {
        return client.findDataByContentCode(topicCode, null)
                .filter(data -> "message".equals(data.getDataType()))
                .sort(Comparator.comparing(Data::getCreationDate))
                .map(data -> new Message(
                        data.getId().toString(),
                        data.getContentNodeCode(),
                        data.getUser(),
                        data.getValue(),
                        Instant.ofEpochMilli(data.getCreationDate())
                ));
    }

    /**
     * Delete all messages for a topic (cleanup)
     */
    /**
     * Delete all messages for a topic (cleanup)
     */
    public Mono<Void> deleteAllMessagesForTopic(String topicCode) {
        System.out.println("  🗑️ Deleting all messages for topic: " + topicCode);

        return client.findDataByContentCode(topicCode, null)
                .filter(data -> "message".equals(data.getDataType()))
                .flatMap(data -> client.deleteDataById(data.getId()))
                .then()
                .doOnSuccess(v -> System.out.println("    ✅ All messages deleted for topic: " + topicCode));
    }

    public Mono<Integer> countMessagesForTopic(String topicCode) {
        return client.findDataByContentCode(topicCode, null)
                .filter(data -> "message".equals(data.getDataType()))
                .count()
                .map(Long::intValue)
                .onErrorReturn(0);
    }

    // ==================== PUBLISH ====================

    public Mono<Void> publishTopic(String topicCode) {
        return client.publishContentNode(topicCode, true)
                .doOnSuccess(p -> System.out.println("  ✅ Topic published: " + topicCode))
                .then();
    }

    // ==================== UTILITIES ====================

    private String extractAuthor(List<Value> values) {
        if (values == null) return "Anonymous";
        return values.stream()
                .filter(v -> "AUTHOR".equals(v.getKey()))
                .findFirst()
                .map(Value::getValue)
                .orElse("Anonymous");
    }

    private String generateTopicCode(String title) {
        String base = title.toUpperCase()
                .replaceAll("[^a-zA-Z0-9]", "-")
                .replaceAll("-+", "-");
        if (base.length() > 20) base = base.substring(0, 20);
        if (base.endsWith("-")) base = base.substring(0, base.length() - 1);
        return "TOPIC-" + base + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}