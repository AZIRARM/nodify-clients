
# Java Client Documentation

## Nodify Reactive Java Client

A reactive Java client for Nodify Headless CMS built with Project Reactor and Netty.

### Features

- ✅ **Reactive Streams** - Built on Project Reactor (Flux/Mono)
- ✅ **Non-blocking I/O** - Powered by Reactor Netty
- ✅ **JWT Authentication** - Automatic token management
- ✅ **Full API Coverage** - All Nodify Core API endpoints
- ✅ **Type Safety** - Complete Java models for all entities
- ✅ **Error Handling** - Custom exception hierarchy
- ✅ **Connection Pooling** - Optimized HTTP connections
- ✅ **Logging** - Built-in SLF4J support

### Installation

#### Maven

```xml
<dependency>
    <groupId>com.itexpert</groupId>
    <artifactId>nodify-client-reactive</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle

```gradle
implementation 'com.itexpert:nodify-client-reactive:1.0.0'
```

### Quick Start

```java
// Create the client
ReactiveNodifyClient client = ReactiveNodifyClient.create(
    ReactiveNodifyClient.builder()
        .withBaseUrl("http://localhost:8880")
        .withTimeout(30000)
        .build()
);

// Authenticate
client.login("admin@example.com", "password123")
    .flatMap(auth -> {
        System.out.println("Authenticated! Token: " + auth.getToken());
        
        // Create a node
        Node node = new Node();
        node.setName("My Site");
        node.setCode("SITE-001");
        node.setSlug("my-site");
        node.setEnvironmentCode("production");
        node.setDefaultLanguage("EN");
        node.setType("SITE");
        
        return client.saveNode(node);
    })
    .subscribe(
        savedNode -> System.out.println("Node created: " + savedNode.getCode()),
        error -> System.err.println("Error: " + error.getMessage())
    );
```

## Core Concepts

### 1. Client Configuration

The client uses a builder pattern for configuration:

```java
ReactiveNodifyClient client = ReactiveNodifyClient.create(
    ReactiveNodifyClient.builder()
        .withBaseUrl("http://localhost:8880")     // Required
        .withTimeout(30000)                        // Optional (default: 30000ms)
        .withAuthToken("your-jwt-token")          // Optional (or use login())
        .withHeader("X-Custom-Header", "value")   // Optional custom headers
        .build()
);
```

### 2. Authentication

Two authentication approaches:

#### Login with credentials

```java
client.login("admin@example.com", "password123")
    .subscribe(authResponse -> {
        String token = authResponse.getToken();
        // Token is automatically stored for subsequent requests
    });
```

#### Set token directly

```java
client.setAuthToken("your-jwt-token");
```

#### Logout

```java
client.logout().subscribe();
```

### 3. Error Handling

All errors are wrapped in `ReactiveNodifyClientException`:

```java
client.findAllNodes()
    .subscribe(
        nodes -> System.out.println("Nodes: " + nodes.size()),
        error -> {
            if (error instanceof ReactiveNodifyClientException) {
                ReactiveNodifyClientException re = (ReactiveNodifyClientException) error;
                System.err.println("HTTP " + re.getStatusCode() + ": " + re.getMessage());
            }
        }
    );
```

### 4. Reactive Types

- **Mono<T>** - For 0-1 result (single object, boolean, etc.)
- **Flux<T>** - For 0-N results (collections, streams)

## API Reference

### Nodes

#### Find all nodes

```java
Flux<Node> nodes = client.findAllNodes();
```

#### Find node by code

```java
Mono<Node> node = client.findNodeByCode("SITE-001");
```

#### Find nodes by parent code

```java
Flux<Node> children = client.findNodesByParentCode("SITE-001");
```

#### Create node

```java
Node node = new Node();
node.setName("My Page");
node.setCode("PAGE-001");
node.setSlug("my-page");
node.setEnvironmentCode("production");
node.setDefaultLanguage("EN");
node.setType("PAGE");
node.setStatus(NodeStatus.NEW);

Mono<Node> saved = client.saveNode(node);
```

#### Update node

```java
node.setDescription("Updated description");
Mono<Node> updated = client.saveNode(node); // Same as create
```

#### Publish node

```java
Mono<Node> published = client.publishNode("PAGE-001");
```

#### Delete node

```java
Mono<Boolean> deleted = client.deleteNode("PAGE-001");
```

#### Delete node definitively

```java
Mono<Boolean> deleted = client.deleteNodeDefinitively("PAGE-001");
```

### Content Nodes

#### Find all content nodes

```java
Flux<ContentNode> contents = client.findAllContentNodes();
```

#### Find content node by code

```java
Mono<ContentNode> content = client.findContentNodeByCode("HTML-001");
```

#### Find content nodes by node code

```java
Flux<ContentNode> contents = client.findContentNodesByNodeCode("PAGE-001");
```

#### Create content node

```java
ContentNode content = new ContentNode();
content.setParentCode("PAGE-001");
content.setCode("HTML-001");
content.setSlug("welcome");
content.setEnvironmentCode("production");
content.setLanguage("EN");
content.setType(ContentNodeType.HTML);
content.setTitle("Welcome Page");
content.setContent("<h1>Hello World</h1>");
content.setStatus(NodeStatus.NEW);

Mono<ContentNode> saved = client.saveContentNode(content);
```

#### Publish content node

```java
Mono<ContentNode> published = client.publishContentNode("HTML-001", true);
```

#### Fill content with payload

```java
ContentNodePayload payload = new ContentNodePayload();
payload.setCode("HTML-001");
payload.setContent("<h1>Updated Content</h1>");

Mono<ContentNode> filled = client.fillContent("HTML-001", "PUBLISHED", payload);
```

#### Export content node

```java
Mono<byte[]> exported = client.exportContentNode("HTML-001", "production");
```

### Data Management

#### Save data

```java
Data data = new Data();
data.setContentNodeCode("HTML-001");
data.setKey("USER_NAME");
data.setValue("John Doe");
data.setDataType("STRING");

Mono<Data> saved = client.saveData(data);
```

#### Find data by key

```java
Mono<Data> data = client.findDataByKey("USER_NAME");
```

#### Find data by content code (with pagination)

```java
PaginationParams params = new PaginationParams(0, 20);
Flux<Data> dataList = client.findDataByContentCode("HTML-001", params);
```

#### Count data by content code

```java
Mono<Long> count = client.countDataByContentCode("HTML-001");
```

#### Delete data by ID

```java
Mono<Boolean> deleted = client.deleteDataById(UUID.fromString("..."));
```

### Translations

#### Create translation

```java
Translation translation = new Translation();
translation.setKey("HELLO_WORLD");
translation.setLanguage("FR");
translation.setValue("Bonjour le monde");

// Translations are typically stored in a ContentNode of type DATA
ContentNode translationContent = new ContentNode();
translationContent.setParentCode("PAGE-001");
translationContent.setType(ContentNodeType.DATA);
translationContent.setTranslations(Arrays.asList(translation));

client.saveContentNode(translationContent);
```

### Values

#### Create value

```java
Value value = new Value();
value.setKey("USER_NAME");
value.setValue("John Doe");

// Values are typically stored in a ContentNode
ContentNode valuesContent = new ContentNode();
valuesContent.setParentCode("PAGE-001");
valuesContent.setType(ContentNodeType.DATA);
valuesContent.setValues(Arrays.asList(value));

client.saveContentNode(valuesContent);
```

### Users

#### Find all users

```java
Flux<UserPost> users = client.findAllUsers();
```

#### Find user by ID

```java
Mono<UserPost> user = client.findUserById(UUID.fromString("..."));
```

#### Find user by email

```java
Mono<UserPost> user = client.findUserByEmail("user@example.com");
```

#### Create user

```java
UserPost user = new UserPost();
user.setEmail("new@example.com");
user.setFirstname("John");
user.setLastname("Doe");
user.setPassword("password123");
user.setRoles(Arrays.asList("USER"));

Mono<UserPost> saved = client.saveUser(user);
```

#### Change password

```java
UserPassword passwordData = new UserPassword();
passwordData.setUserId(userId.toString());
passwordData.setPassword("oldPassword");
passwordData.setNewPassword("newPassword");

Mono<Boolean> changed = client.changePassword(passwordData);
```

### Plugins

#### Find all plugins

```java
Flux<Plugin> plugins = client.findNotDeletedPlugins();
```

#### Enable plugin

```java
Mono<Plugin> enabled = client.enablePlugin(UUID.fromString("..."));
```

#### Disable plugin

```java
Mono<Plugin> disabled = client.disablePlugin(UUID.fromString("..."));
```

#### Export plugin

```java
Mono<byte[]> exported = client.exportPlugin(UUID.fromString("..."));
```

### Locks

#### Acquire lock

```java
Mono<Boolean> acquired = client.acquireLock("resource-123");
```

#### Release lock

```java
Mono<Boolean> released = client.releaseLock("resource-123");
```

#### Get lock owner

```java
Mono<LockInfo> lockInfo = client.getLockOwner("resource-123");
```

#### Get all locks

```java
Flux<LockInfo> locks = client.getAllLocks();
```

### Feedback

#### Find all feedback

```java
Flux<Feedback> feedbacks = client.findAllFeedback();
```

#### Save feedback

```java
Feedback feedback = new Feedback();
feedback.setContentCode("HTML-001");
feedback.setEvaluation(5);
feedback.setMessage("Great content!");
feedback.setUserId("user-123");

Mono<Feedback> saved = client.saveFeedback(feedback);
```

#### Find feedback by content code

```java
Flux<Feedback> feedbacks = client.findFeedbackByContentCode("HTML-001");
```

### Languages

#### Find all languages

```java
Flux<Language> languages = client.findAllLanguages();
```

#### Create language

```java
Language language = new Language();
language.setCode("FR");
language.setName("French");
language.setDescription("French language");

Mono<Language> saved = client.saveLanguage(language);
```

### Environments

#### Find all environments

```java
Flux<Environment> environments = client.findAllEnvironments();
```

#### Create environment

```java
Environment env = new Environment();
env.setCode("staging");
env.setName("Staging");
env.setDescription("Staging environment");

Mono<Environment> saved = client.saveEnvironment(env);
```

### Health

#### Check service health

```java
Mono<String> health = client.health();
health.subscribe(status -> System.out.println("Service status: " + status));
```

## Advanced Examples

### Complete Scenario: Create Node with Translations and Values

```java
public Mono<Void> createMultilingualContent(ReactiveNodifyClient client) {
    // Step 1: Create parent node
    Node parent = new Node();
    parent.setName("Multilingual Site");
    parent.setCode("SITE-ML-" + UUID.randomUUID().toString().substring(0, 8));
    parent.setSlug("multilingual-site");
    parent.setDefaultLanguage("EN");
    parent.setType("SITE");
    
    return client.saveNode(parent)
        .flatMap(savedParent -> {
            // Step 2: Create translations
            ContentNode translations = new ContentNode();
            translations.setParentCode(savedParent.getCode());
            translations.setCode("TRANS-" + UUID.randomUUID().toString().substring(0, 8));
            translations.setType(ContentNodeType.DATA);
            
            List<Translation> transList = Arrays.asList(
                createTranslation("HELLO", "EN", "Hello"),
                createTranslation("HELLO", "FR", "Bonjour"),
                createTranslation("HELLO", "ES", "Hola")
            );
            translations.setTranslations(transList);
            
            return client.saveContentNode(translations)
                .flatMap(savedTrans -> {
                    // Step 3: Create values
                    ContentNode values = new ContentNode();
                    values.setParentCode(savedParent.getCode());
                    values.setCode("VALUES-" + UUID.randomUUID().toString().substring(0, 8));
                    values.setType(ContentNodeType.DATA);
                    
                    Value userName = new Value();
                    userName.setKey("USER_NAME");
                    userName.setValue("John Doe");
                    values.setValues(Arrays.asList(userName));
                    
                    return client.saveContentNode(values)
                        .flatMap(savedValues -> {
                            // Step 4: Create HTML content with directives
                            ContentNode html = new ContentNode();
                            html.setParentCode(savedParent.getCode());
                            html.setCode("HTML-" + UUID.randomUUID().toString().substring(0, 8));
                            html.setType(ContentNodeType.HTML);
                            html.setContent("""
                                <h1>$translate(HELLO), $val(USER_NAME)!</h1>
                                <p>Welcome to our multilingual site.</p>
                            """);
                            
                            return client.saveContentNode(html)
                                .flatMap(savedHtml -> client.publishContentNode(savedHtml.getCode(), true));
                        });
                });
        })
        .then();
}

private Translation createTranslation(String key, String lang, String value) {
    Translation t = new Translation();
    t.setKey(key);
    t.setLanguage(lang);
    t.setValue(value);
    return t;
}
```

### Error Handling with Retry

```java
client.findAllNodes()
    .retry(3) // Retry up to 3 times
    .onErrorResume(error -> {
        System.err.println("Failed after retries: " + error.getMessage());
        return Flux.empty(); // Return empty stream on failure
    })
    .subscribe();
```

### Combining Multiple Operations

```java
Mono.zip(
    client.findNodeByCode("SITE-001"),
    client.findContentNodesByNodeCode("SITE-001").collectList(),
    client.findFeedbackByContentCode("HTML-001").collectList()
)
.map(tuple -> {
    Node node = tuple.getT1();
    List<ContentNode> contents = tuple.getT2();
    List<Feedback> feedbacks = tuple.getT3();
    
    System.out.println("Node: " + node.getName());
    System.out.println("Contents: " + contents.size());
    System.out.println("Feedbacks: " + feedbacks.size());
    
    return node;
})
.subscribe();
```

## Configuration

### Connection Pooling

The client automatically configures connection pooling:

```java
// Default configuration
- Max connections: 100
- Max idle time: 30 seconds
- Max lifetime: 5 minutes
- Pending acquire timeout: 10 seconds
```

### Logging

The client uses `java.util.logging` by default. Configure logging levels:

```properties
# logging.properties
com.itexpert.content.client.level = FINE
reactor.netty.http.client.level = INFO
```

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 400 | Bad Request - Invalid parameters |
| 401 | Unauthorized - Invalid or missing token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 422 | Unprocessable Entity - Validation error |
| 500 | Internal Server Error |

## Dependencies

- Java 11 or higher
- Project Reactor 2022.0.x
- Reactor Netty 1.1.x
- Jackson 2.15.x

