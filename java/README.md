# Nodify Java Client

**Official Java client for Nodify Headless CMS**

[![Maven Central](https://img.shields.io/maven-central/v/io.github.AZIRARM/nodify-java-client.svg)](https://central.sonatype.com/artifact/io.github.AZIRARM/nodify-java-client)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A powerful reactive Java client built with Project Reactor to interact with your Nodify instance, enabling you to manage content, nodes, translations, and more programmatically.

## 📋 Prerequisites

Before using this client, you need access to a **Nodify Headless CMS** instance. Nodify is a powerful, multilingual headless CMS that delivers content through APIs to any channel (websites, mobile apps, IoT, etc.).

### Option 1: Use the Public Demo (For Testing)

You can test the client against the public Nodify demo instance:

*   **Demo URL:** `https://nodify-core.azirar.ovh`
*   **Credentials:**
    *   Username: `admin`
    *   Password: `Admin13579++`

> ⚠️ The demo server is a shared environment and may be reset at any time. It is accessible daily, typically from **10:00 AM to 12:00 AM (UTC+1)**.

### Option 2: Run Nodify Locally with Docker (Recommended for Development)

You can run your own instance of Nodify using Docker Compose. You have two choices depending on whether you have existing MongoDB and Redis instances.

#### **A. Using External MongoDB & Redis**

If you already have MongoDB and Redis running, use this `docker-compose.yml`:

```yaml
services:
  nodify-core:
    image: azirar/nodify-core:latest
    environment:
      MONGO_URL: "mongodb://your-mongo-host:27017/nodify" # Replace with your Mongo URL
      ADMIN_PWD: "YourAdminPassword"                     # Replace with your admin password
      API_URL: "http://nodify-api:1080"                   # Internal URL for the API service
      TZ: "${TZ:-Europe/Paris}"
      REDIS_URL: "redis://your-redis-host:6379"          # Replace with your Redis URL
      JAVA_OPTS: "-Xmx768m -Xms384m"
    ports:
      - "7804:8080"

  nodify-api:
    image: azirar/nodify-api:latest
    environment:
      MONGO_URL: "mongodb://your-mongo-host:27017/nodify" # Replace with your Mongo URL
      TZ: "${TZ:-Europe/Paris}"
      REDIS_URL: "redis://your-redis-host:6379"          # Replace with your Redis URL
      JAVA_OPTS: "-Xmx512m -Xms256m"
    ports:
      - "7805:1080"

  nodify-ui:
    image: azirar/nodify-ui:latest
    depends_on:
      - nodify-core
      - nodify-api
    ports:
      - "7821:80"
    environment:
      CORE_URL: "http://nodify-core:8080"
      API_URL: "http://nodify-api:1080"
```

#### **B. Using Dockerized MongoDB & Redis (All-in-One)**

If you don't have existing databases, this setup will create everything together:

```yaml
services:
  mongo:
    image: mongo:latest
    volumes:
      - mongo-data:/data/db
      - mongo-config:/data/configdb
    ports:
      - "27017:27017"

  redis:
    image: redis:latest
    ports:
      - "6379:6379"

  nodify-core:
    image: azirar/nodify-core:latest
    depends_on:
      - mongo
      - redis
    environment:
      MONGO_URL: "mongodb://mongo:27017/nodify"
      ADMIN_PWD: "Admin123"
      API_URL: "http://nodify-api:1080"
      TZ: "${TZ:-Europe/Paris}"
      REDIS_URL: "redis://redis:6379"
      JAVA_OPTS: "-Xmx768m -Xms384m"
    ports:
      - "7804:8080"

  nodify-api:
    image: azirar/nodify-api:latest
    depends_on:
      - mongo
      - redis
    environment:
      MONGO_URL: "mongodb://mongo:27017/nodify"
      TZ: "${TZ:-Europe/Paris}"
      REDIS_URL: "redis://redis:6379"
      JAVA_OPTS: "-Xmx512m -Xms256m"
    ports:
      - "7805:1080"

  nodify-ui:
    image: azirar/nodify-ui:latest
    depends_on:
      - nodify-core
      - nodify-api
    ports:
      - "7821:80"
    environment:
      CORE_URL: "http://nodify-core:8080"
      API_URL: "http://nodify-api:1080"

volumes:
  mongo-data:
  mongo-config:
```

To start the entire stack:
```bash
docker-compose up -d
```

After running either Docker setup, you can access the Nodify UI at `http://localhost:7821`. The backend services will be available at the mapped ports.

## 📦 Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.AZIRARM</groupId>
    <artifactId>nodify-java-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.AZIRARM:nodify-java-client:1.0.0'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("io.github.AZIRARM:nodify-java-client:1.0.0")
```

## 🚀 Quick Start

Here's a simple example to get you started:

```java
import com.itexpert.content.client.ReactiveNodifyClient;
import com.itexpert.content.lib.models.Node;
import com.itexpert.content.lib.enums.StatusEnum;
import reactor.core.publisher.Mono;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Create a client instance pointing to your Nodify Core instance
        ReactiveNodifyClient client = ReactiveNodifyClient.create(
            ReactiveNodifyClient.builder()
                .withBaseUrl("http://localhost:7804") // URL of your nodify-core instance
                .build()
        );

        // 2. Authenticate and create a node
        client.login("admin", "Admin123")
            .flatMap(authResponse -> {
                System.out.println("✅ Login successful");
                
                // 3. Create a new node (a structural element like a 'Page' or 'Site')
                Node node = new Node();
                node.setName("My First Page");
                node.setCode("MY_FIRST_PAGE");
                node.setSlug("my-first-page");
                node.setType("PAGE");
                node.setStatus(StatusEnum.SNAPSHOT);
                node.setDefaultLanguage("en");
                
                return client.saveNode(node);
            })
            .flatMap(savedNode -> {
                System.out.println("✅ Node created with code: " + savedNode.getCode());
                
                // 4. Find all nodes
                return client.findAllNodes();
            })
            .subscribe(
                nodes -> System.out.println("📊 Total nodes: " + nodes.size()),
                error -> System.err.println("❌ An error occurred: " + error.getMessage())
            );

        // 5. Keep the application running to allow reactive operations to complete
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

## ✨ Core Concepts

### Nodes vs Content Nodes

*   **Node**: Represents a structural element in your content hierarchy (e.g., a `SITE`, a `PAGE`, a `FOLDER`). Nodes can have child nodes and content nodes attached.
*   **Content Node**: Represents the actual content itself. It can be of various types (`HTML`, `JSON`, `DATA`, `FILE`, etc.). It's where your translations and dynamic values live.

### Dynamic Directives

Nodify supports powerful directives within your content strings:

*   **`$translate(KEY)`**: Automatically replaces the directive with the translated value for the current language based on translations attached to the content node.
*   **`$value(KEY)`**: Replaces the directive with a dynamic value stored in the content node's `values` array.

## 📝 Advanced Example: Creating Multilingual Content

This example demonstrates creating a parent node, a child page, and an HTML content node with translations and a dynamic value.

```java
import com.itexpert.content.client.ReactiveNodifyClient;
import com.itexpert.content.lib.enums.ContentTypeEnum;
import com.itexpert.content.lib.enums.StatusEnum;
import com.itexpert.content.lib.models.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

public class CompleteScenario {
    
    public static void main(String[] args) {
        ReactiveNodifyClient client = ReactiveNodifyClient.create(
            ReactiveNodifyClient.builder()
                .withBaseUrl("http://localhost:7804")
                .build()
        );

        client.login("admin", "Admin123")
            .flatMap(auth -> createCompleteScenario(client))
            .subscribe(
                result -> System.out.println("✅ Scenario completed successfully!"),
                error -> System.err.println("❌ Error: " + error.getMessage())
            );

        // Keep the application running
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Mono<Void> createCompleteScenario(ReactiveNodifyClient client) {
        // 1. Create a parent node (e.g., your website)
        return createParentNode(client)
            .flatMap(parentNode -> {
                System.out.println("✅ Parent node created: " + parentNode.getName());

                // 2. Create a child page node under the website
                return createChildNode(client, parentNode.getCode())
                    .flatMap(childNode -> {
                        System.out.println("✅ Child node created: " + childNode.getName());

                        // 3. Create HTML content with translations and values
                        return createHtmlContent(client, childNode.getCode())
                            .flatMap(contentNode -> {
                                // 4. Add translations
                                return addTranslations(client, contentNode)
                                    // 5. Add user name value
                                    .flatMap(nodeWithTranslations -> addUserNameValue(client, nodeWithTranslations))
                                    .flatMap(finalContent -> {
                                        System.out.println("✅ HTML content created with translations and values");

                                        // 6. Publish the content
                                        return publishContent(client, finalContent.getCode())
                                            .flatMap(published -> {
                                                System.out.println("✅ Content published");

                                                // 7. Publish the parent node
                                                return publishNode(client, parentNode.getCode())
                                                    .map(publishedNode -> {
                                                        System.out.println("✅ Parent node published");
                                                        displayFinalInfo(parentNode, childNode, finalContent);
                                                        return publishedNode;
                                                    });
                                            });
                                    });
                            });
                    });
            })
            .then();
    }

    private static Mono<Node> createParentNode(ReactiveNodifyClient client) {
        Node parentNode = new Node();
        parentNode.setName("My Multilingual Site");
        parentNode.setCode("MY_SITE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parentNode.setSlug("my-site");
        parentNode.setType("SITE");
        parentNode.setStatus(StatusEnum.SNAPSHOT);
        parentNode.setDefaultLanguage("en");
        parentNode.setEnvironmentCode("production");
        
        return client.saveNode(parentNode);
    }

    private static Mono<Node> createChildNode(ReactiveNodifyClient client, String parentCode) {
        Node childNode = new Node();
        childNode.setParentCode(parentCode);
        childNode.setName("Welcome Page");
        childNode.setCode("WELCOME_PAGE_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        childNode.setSlug("welcome");
        childNode.setType("PAGE");
        childNode.setStatus(StatusEnum.SNAPSHOT);
        childNode.setDefaultLanguage("en");
        childNode.setEnvironmentCode("production");
        
        return client.saveNode(childNode);
    }

    private static Mono<ContentNode> createHtmlContent(ReactiveNodifyClient client, String nodeCode) {
        ContentNode contentNode = new ContentNode();
        contentNode.setParentCode(nodeCode);
        contentNode.setType(ContentTypeEnum.HTML);
        contentNode.setTitle("Dynamic Welcome");
        contentNode.setCode("WELCOME_HTML_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        contentNode.setSlug("welcome-html");
        contentNode.setLanguage("en");
        contentNode.setStatus(StatusEnum.SNAPSHOT);
        contentNode.setEnvironmentCode("production");
        contentNode.setContent(
            "<h1>$translate(GREETING), $value(USER_NAME)!</h1>\n" +
            "<p>Welcome to your Nodify site built with Java.</p>"
        );
        
        return client.saveContentNode(contentNode);
    }

    private static Mono<ContentNode> addTranslations(ReactiveNodifyClient client, ContentNode contentNode) {
        Translation enGreeting = new Translation();
        enGreeting.setKey("GREETING");
        enGreeting.setLanguage("en");
        enGreeting.setValue("Hello");
        
        Translation frGreeting = new Translation();
        frGreeting.setKey("GREETING");
        frGreeting.setLanguage("fr");
        frGreeting.setValue("Bonjour");
        
        Translation esGreeting = new Translation();
        esGreeting.setKey("GREETING");
        esGreeting.setLanguage("es");
        esGreeting.setValue("Hola");
        
        contentNode.setTranslations(Arrays.asList(enGreeting, frGreeting, esGreeting));
        
        return client.saveContentNode(contentNode);
    }

    private static Mono<ContentNode> addUserNameValue(ReactiveNodifyClient client, ContentNode contentNode) {
        Value userNameValue = new Value();
        userNameValue.setKey("USER_NAME");
        userNameValue.setValue("Java Developer");
        
        contentNode.setValues(Arrays.asList(userNameValue));
        
        return client.saveContentNode(contentNode);
    }

    private static Mono<ContentNode> publishContent(ReactiveNodifyClient client, String contentCode) {
        return client.publishContentNode(contentCode, true);
    }

    private static Mono<Node> publishNode(ReactiveNodifyClient client, String nodeCode) {
        return client.publishNode(nodeCode);
    }

    private static void displayFinalInfo(Node parentNode, Node childNode, ContentNode contentNode) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎯 FINAL SCENARIO SUMMARY");
        System.out.println("=".repeat(60));
        
        System.out.println("\n📁 PARENT NODE:");
        System.out.println("   - Name: " + parentNode.getName());
        System.out.println("   - Code: " + parentNode.getCode());
        
        System.out.println("\n📄 CHILD NODE:");
        System.out.println("   - Name: " + childNode.getName());
        System.out.println("   - Code: " + childNode.getCode());
        System.out.println("   - Parent: " + childNode.getParentCode());
        
        System.out.println("\n📝 CONTENT NODE:");
        System.out.println("   - Code: " + contentNode.getCode());
        System.out.println("   - Title: " + contentNode.getTitle());
        
        System.out.println("\n🌍 TRANSLATIONS:");
        if (contentNode.getTranslations() != null) {
            contentNode.getTranslations().forEach(t -> 
                System.out.println("   - " + t.getKey() + " [" + t.getLanguage() + "]: " + t.getValue())
            );
        }
        
        System.out.println("\n🔢 VALUES:");
        if (contentNode.getValues() != null) {
            contentNode.getValues().forEach(v -> 
                System.out.println("   - " + v.getKey() + ": " + v.getValue())
            );
        }
        
        System.out.println("\n" + "=".repeat(60));
    }
}
```

## 📖 API Reference

The client provides methods for all Nodify API endpoints, organized by resource:

### Authentication
* `Mono<AuthResponse> login(String email, String password)`
* `Mono<Void> logout()`

### Nodes
* `Flux<Node> findAllNodes()`
* `Mono<Node> saveNode(Node node)`
* `Mono<Node> findNodeByCode(String code)`
* `Mono<Node> findNodeByCodeAndStatus(String code, String status)`
* `Flux<Node> findNodesByParentCode(String code)`
* `Mono<Node> publishNode(String code)`
* `Mono<Boolean> deleteNode(String code)`
* `Mono<Boolean> deleteNodeDefinitively(String code)`

### Content Nodes
* `Flux<ContentNode> findAllContentNodes()`
* `Mono<ContentNode> saveContentNode(ContentNode contentNode)`
* `Mono<ContentNode> findContentNodeByCodeAndStatus(String code, String status)`
* `Flux<ContentNode> findContentNodesByNodeCode(String code)`
* `Mono<ContentNode> publishContentNode(String code, boolean publish)`
* `Mono<Boolean> deleteContentNode(String code)`

### Users
* `Flux<UserPost> findAllUsers()`
* `Mono<UserPost> saveUser(UserPost user)`
* `Mono<UserPost> findUserByEmail(String email)`
* `Mono<Boolean> changePassword(UserPassword passwordData)`
* `Mono<Boolean> deleteUser(UUID id)`

### Plugins
* `Flux<Plugin> findNotDeletedPlugins()`
* `Mono<Plugin> savePlugin(Plugin plugin)`
* `Mono<Plugin> enablePlugin(UUID id)`
* `Mono<Plugin> disablePlugin(UUID id)`
* `Mono<byte[]> exportPlugin(UUID id)`

### Feedback
* `Flux<Feedback> findAllFeedback()`
* `Mono<Feedback> saveFeedback(Feedback feedback)`
* `Flux<FeedbackCharts> getContentCharts()`

### Locks
* `Mono<Boolean> acquireLock(String code)`
* `Mono<Boolean> releaseLock(String code)`
* `Mono<LockInfo> getLockOwner(String code)`
* `Flux<LockInfo> getAllLocks()`

### Data
* `Mono<Data> saveData(Data data)`
* `Mono<Data> findDataByKey(String key)`
* `Flux<Data> findDataByContentCode(String code, PaginationParams params)`

## ⚙️ Configuration

### Client Builder Options

```java
ReactiveNodifyClient client = ReactiveNodifyClient.create(
    ReactiveNodifyClient.builder()
        .withBaseUrl("https://your-nodify-instance.com")
        .withTimeout(30000)                    // Timeout in milliseconds
        .withAuthToken("your-jwt-token")       // Optional: pre-authenticated token
        .withHeader("X-Custom-Header", "value") // Custom headers
        .build()
);
```

### Pagination

```java
PaginationParams params = new PaginationParams(1, 20); // Page 1, 20 items per page
Flux<Data> data = client.findDataByContentCode("CONTENT_CODE", params);
```

## 📋 Requirements

- Java 8 or higher
- Project Reactor (included as dependency)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## 📄 License

This client library is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.