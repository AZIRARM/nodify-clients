# Nodify Clients

Official Nodify API clients for multiple programming languages. Nodify is a powerful content management system that enables dynamic content delivery with built-in translation and personalization features.

## 🚀 Available Clients

| Language | Status | Package |
|----------|--------|---------|
| **Java** | ✅ Stable | [![Maven Central](https://img.shields.io/maven-central/v/com.itexpert/nodify-client)](https://mvnrepository.com/artifact/com.itexpert/nodify-client) |
| **Node.js** | ✅ Stable | [![npm](https://img.shields.io/npm/v/nodify-node-client)](https://www.npmjs.com/package/nodify-node-client) |
| **Python** | ✅ Stable | [![PyPI](https://img.shields.io/pypi/v/nodify-python-client)](https://pypi.org/project/nodify-python-client/) |
| **Kotlin** | 🔄 Coming Soon | - |

## 📦 Installation

### Java (Maven)
```xml
<dependency>
    <groupId>com.itexpert</groupId>
    <artifactId>nodify-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Java (Gradle)
```gradle
implementation 'com.itexpert:nodify-client:1.0.0'
```

### Node.js
```bash
npm install nodify-node-client
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

## 🔧 Quick Start

### Java Example
```java
import com.itexpert.content.client.ReactiveNodifyClient;
import com.itexpert.content.lib.models.Node;
import com.itexpert.content.lib.models.ContentNode;
import com.itexpert.content.lib.enums.ContentTypeEnum;
import com.itexpert.content.lib.enums.StatusEnum;

public class QuickStart {
    public static void main(String[] args) {
        // Create reactive client
        ReactiveNodifyClient client = ReactiveNodifyClient.create(
            ReactiveNodifyClient.builder()
                .withBaseUrl("https://nodify-core.azirar.ovh")
                .withTimeout(30000)
                .build()
        );
        
        // Login
        client.login("admin", "Admin13579++")
            .flatMap(auth -> {
                System.out.println("✅ Authenticated");
                
                // Create a node
                Node node = new Node();
                node.setName("My English Website written with Java");
                node.setCode("SITE-EN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                node.setSlug("my-english-website");
                node.setEnvironmentCode("production");
                node.setDefaultLanguage("EN");
                node.setType("SITE");
                node.setStatus(StatusEnum.SNAPSHOT);
                
                return client.saveNode(node);
            })
            .subscribe(
                saved -> System.out.println("Node created: " + saved.getCode()),
                error -> System.err.println("Error: " + error.getMessage())
            );
    }
}
```

### Node.js Example
```typescript
import { NodifyClient, Node, ContentNode, ContentNodeType } from 'nodify-node-client';

async function quickStart() {
    // Create client
    const client = NodifyClient.builder()
        .withBaseUrl('https://nodify-core.azirar.ovh')
        .build();

    // Login
    await client.login('admin', 'Admin13579++');

    // Create a node
    const node: Node = {
        name: 'My English Website written with Node.js',
        code: `SITE-EN-${Math.random().toString(36).substring(2, 10).toUpperCase()}`,
        slug: 'my-english-website',
        environmentCode: 'production',
        defaultLanguage: 'EN',
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
import uuid
from nodify_client import (
    ReactiveNodifyClient,
    ReactiveNodifyClientConfig,
    Node,
    StatusEnum
)

async def quick_start():
    # Create reactive client
    client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .with_base_url("https://nodify-core.azirar.ovh")
            .with_timeout(30000)
            .build()
    )

    try:
        # Login
        auth = await client.login("admin", "Admin13579++")
        print("✅ Authenticated")
        
        # Create a node
        node = Node(
            name="My English Website written with Python",
            code=f"SITE-EN-{str(uuid.uuid4())[:8].upper()}",
            slug="my-english-website",
            environment_code="production",
            default_language="EN",
            type="SITE",
            status=StatusEnum.SNAPSHOT
        )
        
        saved = await client.save_node(node)
        print(f"Node created: {saved.code}")
        
    finally:
        await client.close()

asyncio.run(quick_start())
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

### Hierarchy Example
```
My English Website (SITE-EN-XXXX) [Node]
└── Welcome Page (PAGE-WELCOME-XXXX) [Node]
    └── Welcome Message (HTML-XXXX) [Content Node with translations & values]
```

## 🛠️ Advanced Examples

### Complete Scenario (Java)
```java
private static Mono<Void> createCompleteScenario(ReactiveNodifyClient client) {
    return createParentNode(client)
        .flatMap(parentNode -> createChildNode(client, parentNode.getCode())
            .flatMap(childNode -> createHtmlContent(client, childNode.getCode())
                .flatMap(contentNode -> createTranslations(client, contentNode))
                .flatMap(contentNode -> createUserNameValue(client, contentNode))
                .flatMap(content -> publishContent(client, content.getCode())
                    .flatMap(published -> publishNode(client, parentNode.getCode()))))));
}
```

### Complete Scenario (Python)
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

### Reactive Programming (Node.js)
```typescript
const nodes = await client.findAllNodes();
const contentNodes = await Promise.all(
    nodes
        .filter(node => node.type === 'PAGE')
        .map(node => client.findContentNodesByNodeCode(node.code))
);
```

## 📖 Documentation

Full documentation is available at [https://github.com/AZIRARM/nodify/wiki](https://github.com/AZIRARM/nodify/wiki)

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.