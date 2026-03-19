# Nodify Clients

Official Nodify API clients for multiple programming languages. Nodify is a powerful content management system that enables dynamic content delivery with built-in translation and personalization features.

## 🚀 Available Clients

| Language | Status | Package |
|----------|--------|---------|
| **Java** | ✅ Stable | [![Maven Central](https://img.shields.io/maven-central/v/io.github.azirarm/nodify-java-client)](https://central.sonatype.com/artifact/io.github.azirarm/nodify-java-client) |
| **Node.js** | ✅ Stable | [![npm](https://img.shields.io/npm/v/nodify-node-client)](https://www.npmjs.com/package/nodify-node-client) |
| **Python** | ✅ Stable | [![PyPI version](https://img.shields.io/pypi/v/nodify-python-client.svg)](https://pypi.org/project/nodify-python-client/) |
| **PHP** | ✅ Stable | [![Packagist](https://img.shields.io/packagist/v/nodify/php-client)](https://packagist.org/packages/nodify/php-client) |

> **Note**: Kotlin developers can use the Java client directly with full interoperability.

## 📦 Installation

### Java (Maven)
```xml
<dependency>
    <groupId>io.github.azirarm</groupId>
    <artifactId>nodify-java-client</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Java (Gradle)
```gradle
implementation 'io.github.azirarm:nodify-java-client:1.1.0'
```

### Kotlin (using Java client)
```kotlin
// In build.gradle.kts
dependencies {
    implementation("io.github.azirarm:nodify-java-client:1.1.0")
}
```

### Node.js
```bash
npm install @azirarm/nodify-node-client
```

### Python
```bash
# Install from PyPI
pip install nodify-python-client

# Or install in development mode
git clone https://github.com/AZIRARM/nodify-clients.git
cd nodify-clients/python
pip install -e .
```

### PHP
```bash
# Install using Composer
composer require nodify/php-client

# Or install in development mode (from the dedicated repository)
git clone https://github.com/AZIRARM/nodify-php-client.git
cd nodify-php-client
composer install
```

> **Note**: The PHP client is maintained in its own repository at [https://github.com/AZIRARM/nodify-php-client](https://github.com/AZIRARM/nodify-php-client)

## 🔧 Quick Start

### Java Example
```java
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.Node;

public class QuickStart {
    public static void main(String[] args) {
        ReactiveNodifyClient client = ReactiveNodifyClient.create(
            ReactiveNodifyClient.builder()
                .withBaseUrl("https://nodify-core.azirar.ovh")
                .build()
        );
        
        client.login("admin", "Admin13579++")
            .flatMap(auth -> {
                Node node = new Node();
                node.setName("My Website written with Java");
                node.setCode("SITE-EN-JAVA");
                node.setType("SITE");
                node.setStatus(StatusEnum.SNAPSHOT);
                return client.saveNode(node);
            })
            .subscribe(
                saved -> System.out.println("Node created: " + saved.getCode()),
                error -> System.err.println("Error: " + error)
            );
    }
}
```

### Kotlin Example (using Java client)
```kotlin
import io.github.azirarm.content.client.ReactiveNodifyClient
import io.github.azirarm.content.lib.enums.StatusEnum
import io.github.azirarm.content.lib.models.Node

fun main() {
    val client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .withBaseUrl("https://nodify-core.azirar.ovh")
            .build()
    )
    
    client.login("admin", "Admin13579++")
        .flatMap { auth ->
            val node = Node().apply {
                name = "My Website written with Kotlin"
                code = "SITE-EN-KOTLIN"
                type = "SITE"
                status = StatusEnum.SNAPSHOT
            }
            client.saveNode(node)
        }
        .subscribe(
            { saved -> println("Node created: ${saved.code}") },
            { error -> println("Error: $error") }
        )
}
```

### Node.js Example
```typescript
import { NodifyClient, Node } from '@azirarm/nodify-node-client';

async function quickStart() {
    const client = NodifyClient.builder()
        .withBaseUrl('https://nodify-core.azirar.ovh')
        .build();

    await client.login('admin', 'Admin13579++');

    const node: Node = {
        name: 'My Website written with Node.js',
        code: 'SITE-EN-NODE',
        type: 'SITE',
        status: 'SNAPSHOT'
    };

    const saved = await client.saveNode(node);
    console.log('Node created:', saved.code);
}

quickStart();
```

### Python Example
```python
import asyncio
from nodify_client import ReactiveNodifyClient, Node, StatusEnum

async def quick_start():
    client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .with_base_url("https://nodify-core.azirar.ovh")
            .build()
    )

    try:
        await client.login("admin", "Admin13579++")
        
        node = Node(
            name="My Website written with Python",
            code="SITE-EN-PYTHON",
            type="SITE",
            status=StatusEnum.SNAPSHOT
        )
        
        saved = await client.save_node(node)
        print(f"Node created: {saved.code}")
        
    finally:
        await client.close()

asyncio.run(quick_start())
```

### PHP Example
```php
<?php

require_once 'vendor/autoload.php';

use Nodify\Client;
use Nodify\Node;
use Nodify\StatusEnum;

$client = Client::create(
    Client::builder()
        ->withBaseUrl('https://nodify-core.azirar.ovh')
        ->build()
);

$auth = $client->login('admin', 'Admin13579++');

$node = new Node();
$node->name = "My Website written with PHP";
$node->code = "SITE-EN-PHP";
$node->type = "SITE";
$node->status = StatusEnum::SNAPSHOT;

$saved = $client->saveNode($node);
echo "Node created: " . $saved->code . "\n";
```

## ✨ Key Features

- **🔐 Authentication** - Secure JWT token management
- **📄 Node Management** - Create, read, update, delete content nodes
- **🌍 Multi-language** - Built-in translation support with `$translate` directive
- **🎯 Dynamic Content** - Personalization with `$value` directive
- **📊 Analytics** - Track content displays and clicks
- **🔒 Lock System** - Prevent concurrent modifications
- **📦 Import/Export** - Bulk operations support
- **🔌 Plugin System** - Extend functionality with plugins
- **⚡ Reactive/Async** - All clients support reactive/async programming

## 📚 Core Concepts

### Nodes vs Content Nodes
- **Node**: Structural element (Site, Page, Container) - organizes your content hierarchy
- **Content Node**: Actual content (HTML, Text, JSON, Data) - the content itself with translations and values

### Dynamic Directives
- **$translate(KEY)** - Automatically translates content based on user language
- **$value(KEY)** - Inserts dynamic values for personalization

## 🛠️ Complete Scenario Example

### Java
```java
private static Mono<Void> createCompleteScenario(ReactiveNodifyClient client) {
    return createParentNode(client)
        .flatMap(parent -> createChildNode(client, parent.getCode())
            .flatMap(child -> createHtmlContent(client, child.getCode())
                .flatMap(content -> createTranslations(client, content))
                .flatMap(content -> createUserNameValue(client, content))
                .flatMap(content -> publishContent(client, content.getCode())
                    .flatMap(pub -> publishNode(client, parent.getCode())))));
}
```

### Kotlin
```kotlin
private fun createCompleteScenario(client: ReactiveNodifyClient): Mono<Void> {
    return createParentNode(client)
        .flatMap { parent ->
            createChildNode(client, parent.code)
                .flatMap { child ->
                    createHtmlContent(client, child.code)
                        .flatMap { content ->
                            createTranslations(client, content)
                                .flatMap { createUserNameValue(client, it) }
                                .flatMap { contentWithValues ->
                                    publishContent(client, contentWithValues.code)
                                        .flatMap { publishNode(client, parent.code) }
                                }
                        }
                }
        }.then()
}
```

### Node.js
```typescript
async function createCompleteScenario(client: NodifyClient) {
    const parent = await createParentNode(client);
    const child = await createChildNode(client, parent.code!);
    let content = await createHtmlContent(client, child.code!);
    content = await createTranslations(client, content);
    content = await createUserNameValue(client, content);
    await publishContent(client, content.code!);
    await publishNode(client, parent.code!);
}
```

### Python
```python
async def create_complete_scenario(client):
    parent = await create_parent_node(client)
    child = await create_child_node(client, parent.code)
    content = await create_html_content(client, child.code)
    content = await create_translations(client, content)
    content = await create_user_name_value(client, content)
    await publish_content(client, content.code)
    await publish_node(client, parent.code)
```

### PHP
```php
function createCompleteScenario($client) {
    $parent = createParentNode($client);
    $child = createChildNode($client, $parent->code);
    $content = createHtmlContent($client, $child->code);
    $content = createTranslations($client, $content);
    $content = createUserNameValue($client, $content);
    publishContent($client, $content->code);
    publishNode($client, $parent->code);
}
```

## 📖 Documentation

Full documentation is available at [https://github.com/AZIRARM/nodify/wiki](https://github.com/AZIRARM/nodify/wiki)

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
