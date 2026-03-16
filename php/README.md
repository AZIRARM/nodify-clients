# Nodify PHP Client

**Official PHP client for Nodify Headless CMS**

[![Packagist Version](https://img.shields.io/packagist/v/nodify/php-client)](https://packagist.org/packages/nodify/php-client)
[![PHP Version](https://img.shields.io/packagist/php-v/nodify/php-client)](https://packagist.org/packages/nodify/php-client)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A powerful PHP client built with Guzzle to interact with your Nodify instance, enabling you to manage content, nodes, translations, and more programmatically.

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

### Using Composer

```bash
composer require nodify/php-client
```

### Manual installation (development)

```bash
git clone https://github.com/AZIRARM/nodify-clients.git
cd nodify-clients/php
composer install
```

## 🚀 Quick Start

Here's a simple example to get you started:

```php
<?php

require_once 'vendor/autoload.php';

use Nodify\Client;
use Nodify\Node;
use Nodify\StatusEnum;

// 1. Create a client instance pointing to your Nodify Core instance
$client = Client::create(
    Client::builder()
        ->withBaseUrl('http://localhost:7804')  // URL of your nodify-core instance
        ->build()
);

try {
    // 2. Authenticate
    $auth = $client->login('admin', 'Admin123');
    echo "✅ Login successful\n";

    // 3. Create a new node (a structural element like a 'Page' or 'Site')
    $node = new Node();
    $node->name = "My First Page";
    $node->code = "MY_FIRST_PAGE_" . strtoupper(uniqid());
    $node->slug = "my-first-page";
    $node->type = "PAGE";
    $node->status = StatusEnum::SNAPSHOT;
    $node->defaultLanguage = "en";
    $node->environmentCode = "production";

    $savedNode = $client->saveNode($node);
    echo "✅ Node created with code: {$savedNode->code}\n";

    // 4. Find all nodes
    $allNodes = $client->findAllNodes();
    echo "📊 Total nodes: " . count($allNodes) . "\n";

} catch (Exception $e) {
    echo "❌ An error occurred: " . $e->getMessage() . "\n";
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

```php
<?php

require_once 'vendor/autoload.php';

use Nodify\Client;
use Nodify\Node;
use Nodify\ContentNode;
use Nodify\Translation;
use Nodify\Value;
use Nodify\ContentTypeEnum;
use Nodify\StatusEnum;

function createCompleteScenario() {
    // 1. Create a client instance
    $client = Client::create(
        Client::builder()
            ->withBaseUrl('http://localhost:7804')
            ->build()
    );

    try {
        // 2. Login
        $client->login('admin', 'Admin123');
        echo "✅ Login successful\n";

        // 3. Create a parent node (e.g., your website)
        $parentNode = new Node();
        $parentNode->name = "My Multilingual Site";
        $parentNode->code = "MY_SITE_" . strtoupper(uniqid());
        $parentNode->slug = "my-site";
        $parentNode->type = "SITE";
        $parentNode->status = StatusEnum::SNAPSHOT;
        $parentNode->defaultLanguage = "en";
        $parentNode->environmentCode = "production";
        
        $savedParent = $client->saveNode($parentNode);
        echo "✅ Parent node created: {$savedParent->name}\n";

        // 4. Create a child page node under the website
        $childNode = new Node();
        $childNode->parentCode = $savedParent->code;
        $childNode->name = "Welcome Page";
        $childNode->code = "WELCOME_PAGE_" . strtoupper(uniqid());
        $childNode->slug = "welcome";
        $childNode->type = "PAGE";
        $childNode->status = StatusEnum::SNAPSHOT;
        $childNode->defaultLanguage = "en";
        $childNode->environmentCode = "production";
        
        $savedChild = $client->saveNode($childNode);
        echo "✅ Child node created: {$savedChild->name}\n";

        // 5. Create HTML content with translations and values
        $translations = [
            new Translation("GREETING", "en", "Hello"),
            new Translation("GREETING", "fr", "Bonjour"),
            new Translation("GREETING", "es", "Hola"),
        ];

        $values = [
            new Value("USER_NAME", "PHP Developer")
        ];

        $contentNode = new ContentNode();
        $contentNode->parentCode = $savedChild->code;
        $contentNode->type = ContentTypeEnum::HTML;
        $contentNode->title = "Dynamic Welcome";
        $contentNode->code = "WELCOME_HTML_" . strtoupper(uniqid());
        $contentNode->slug = "welcome-html";
        $contentNode->language = "en";
        $contentNode->status = StatusEnum::SNAPSHOT;
        $contentNode->environmentCode = "production";
        $contentNode->content = "<h1>\$translate(GREETING), \$value(USER_NAME)!</h1>\n<p>Welcome to your Nodify site built with PHP.</p>";
        $contentNode->translations = $translations;
        $contentNode->values = $values;

        $savedContent = $client->saveContentNode($contentNode);
        echo "✅ Content node created: {$savedContent->title}\n";

        // 6. Publish the content
        $client->publishContentNode($savedContent->code, true);
        echo "✅ Content published\n";

        // 7. Publish the parent node
        $client->publishNode($savedParent->code);
        echo "✅ Parent node published\n";

        // 8. Display final information
        echo "\n" . str_repeat("=", 60) . "\n";
        echo "🎯 FINAL SCENARIO SUMMARY\n";
        echo str_repeat("=", 60) . "\n";
        
        echo "\n📁 PARENT NODE: {$savedParent->name} ({$savedParent->code})\n";
        echo "📄 CHILD NODE: {$savedChild->name} ({$savedChild->code})\n";
        echo "📝 CONTENT NODE: {$savedContent->title} ({$savedContent->code})\n";
        
        echo "\n🌍 TRANSLATIONS:\n";
        foreach ($savedContent->translations as $t) {
            echo "   - {$t->key} [{$t->language}]: {$t->value}\n";
        }
        
        echo "\n🔢 VALUES:\n";
        foreach ($savedContent->values as $v) {
            echo "   - {$v->key}: {$v->value}\n";
        }

    } catch (Exception $e) {
        echo "❌ An error occurred: " . $e->getMessage() . "\n";
    }
}

createCompleteScenario();
```

## 📖 API Reference

The client provides methods for all Nodify API endpoints, organized by resource:

### Authentication
* `login(string $email, string $password): AuthResponse`
* `logout(): void`

### Nodes
* `findAllNodes(): array`
* `saveNode(Node $node): Node`
* `findNodeByCode(string $code): Node`
* `findNodeByCodeAndStatus(string $code, string $status): Node`
* `findNodesByParentCode(string $code): array`
* `publishNode(string $code): Node`
* `deleteNode(string $code): bool`

### Content Nodes
* `findAllContentNodes(): array`
* `saveContentNode(ContentNode $contentNode): ContentNode`
* `findContentNodeByCodeAndStatus(string $code, string $status): ContentNode`
* `findContentNodesByNodeCode(string $code): array`
* `publishContentNode(string $code, bool $publish): ContentNode`
* `deleteContentNode(string $code): bool`

### Users
* `findAllUsers(): array`
* `saveUser(UserPost $user): UserPost`
* `findUserByEmail(string $email): UserPost`
* `changePassword(UserPassword $passwordData): bool`
* `deleteUser(string $userId): bool`

### Plugins
* `findNotDeletedPlugins(): array`
* `savePlugin(Plugin $plugin): Plugin`
* `enablePlugin(string $pluginId): Plugin`
* `disablePlugin(string $pluginId): Plugin`
* `exportPlugin(string $pluginId): string`

### Feedback
* `findAllFeedback(): array`
* `saveFeedback(Feedback $feedback): Feedback`
* `getContentCharts(): array`

### Locks
* `acquireLock(string $code): bool`
* `releaseLock(string $code): bool`
* `getLockOwner(string $code): LockInfo`
* `getAllLocks(): array`

### Data
* `saveData(Data $data): Data`
* `findDataByKey(string $key): Data`
* `findDataByContentCode(string $code, ?PaginationParams $params = null): array`

## ⚙️ Configuration

### Client Builder Options

```php
$client = Client::create(
    Client::builder()
        ->withBaseUrl('https://your-nodify-instance.com')
        ->withTimeout(30000)                    // Timeout in milliseconds
        ->withAuthToken('your-jwt-token')       // Optional: pre-authenticated token
        ->withHeader('X-Custom-Header', 'value') // Custom headers
        ->build()
);
```

### Pagination

```php
use Nodify\PaginationParams;

$params = new PaginationParams(1, 20); // Page 1, 20 items per page
$data = $client->findDataByContentCode('CONTENT_CODE', $params);
```

## 📋 Requirements

- PHP 8.0 or higher
- GuzzleHTTP 7.0 or higher
- Composer

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## 📄 License

This client library is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.