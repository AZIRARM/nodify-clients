# Nodify Clients

Official Nodify API clients for multiple programming languages. Nodify is a powerful content management system that enables dynamic content delivery with built-in translation and personalization features.

## 🚀 Available Clients

| Language | Status | Package |
|----------|--------|---------|
| **Java** | ✅ Stable | [![Maven Central](https://img.shields.io/maven-central/v/com.itexpert/nodify-client)](https://mvnrepository.com/artifact/com.itexpert/nodify-client) |
| **Node.js** | ✅ Stable | [![npm](https://img.shields.io/npm/v/nodify-node-client)](https://www.npmjs.com/package/nodify-node-client) |
| **Python** | 🔄 Coming Soon | - |
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

## 🔧 Quick Start

### Java Example
```java
import com.itexpert.nodify.client.NodifyClient;
import com.itexpert.nodify.client.models.Node;
import com.itexpert.nodify.client.models.ContentNode;

public class QuickStart {
    public static void main(String[] args) {
        // Create client
        NodifyClient client = NodifyClient.builder()
                .baseUrl("https://api.nodify.io")
                .apiKey("YOUR_API_KEY")
                .build();
        
        // Login
        client.login("admin@example.com", "password");
        
        // Create a node
        Node node = new Node();
        node.setName("My Page");
        node.setCode("PAGE-001");
        node.setSlug("my-page");
        node.setType("PAGE");
        
        Node saved = client.saveNode(node);
        System.out.println("Node created: " + saved.getCode());
        
        // Create content with translation
        ContentNode content = new ContentNode();
        content.setParentCode(saved.getCode());
        content.setType(ContentTypeEnum.HTML);
        content.setTitle("Welcome");
        content.setContent("<h1>$translate(HELLO)</h1>");
        
        // Add translations
        content.getTranslations().add(new Translation()
            .setKey("HELLO")
            .setLanguage("EN")
            .setValue("Hello World"));
        
        content.getTranslations().add(new Translation()
            .setKey("HELLO")
            .setLanguage("FR")
            .setValue("Bonjour le monde"));
        
        ContentNode savedContent = client.saveContentNode(content);
        
        // Publish
        client.publishContentNode(savedContent.getCode(), true);
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
        .withHeader('X-API-Key', 'YOUR_API_KEY')
        .build();

    // Login
    await client.login('admin@example.com', 'Admin13579++');

    // Create a node
    const node: Node = {
        name: 'My Page',
        code: 'PAGE-001',
        slug: 'my-page',
        type: 'PAGE',
        environmentCode: 'default',
        defaultLanguage: 'EN'
    };

    const saved = await client.saveNode(node);
    console.log('Node created:', saved.code);

    // Create content with translation
    const content: ContentNode = {
        parentCode: saved.code,
        type: ContentNodeType.HTML,
        title: 'Welcome',
        content: '<h1>$translate(HELLO)</h1>',
        code: 'CONTENT-001',
        slug: 'welcome',
        environmentCode: 'default',
        language: 'EN',
        translations: [
            { key: 'HELLO', language: 'EN', value: 'Hello World' },
            { key: 'HELLO', language: 'FR', value: 'Bonjour le monde' }
        ]
    };

    const savedContent = await client.saveContentNode(content);

    // Publish
    await client.publishContentNode(savedContent.code!, true);
}

quickStart();
```

## ✨ Key Features

- **🔐 Authentication** - Secure API key and JWT token management
- **📄 Node Management** - Create, read, update, delete content nodes
- **🌍 Multi-language** - Built-in translation support with $translate directive
- **🎯 Dynamic Content** - Personalization with $value directive
- **📊 Analytics** - Track content displays and clicks
- **🔒 Lock System** - Prevent concurrent modifications
- **📦 Import/Export** - Bulk operations support
- **🔌 Plugin System** - Extend functionality with plugins

## 📚 Core Concepts

### Nodes vs Content Nodes
- **Node**: Structural element (Site, Page, Container) - organizes your content hierarchy
- **Content Node**: Actual content (HTML, Text, JSON, Data) - the content itself with translations and values

### Dynamic Directives
- **$translate(KEY)** - Automatically translates content based on user language
- **$value(KEY)** - Inserts dynamic values for personalization

## 🛠️ Advanced Examples

### Fluent API (Java)
```java
nodifyClient
    .checkIfNodeExist("PAGE-001")
    .ifExists(node -> {
        node.setTitle("Updated Title");
    })
    .ifNotExists(() -> {
        // Create new node
    })
    .save()
    .publish();
```

### Reactive Programming (Java)
```java
client.login("admin", "password")
    .flatMap(auth -> client.findAllNodes())
    .flatMapMany(Flux::fromIterable)
    .filter(node -> "PAGE".equals(node.getType()))
    .subscribe(node -> System.out.println(node.getName()));
```

### Batch Operations (Node.js)
```typescript
const nodes = await client.importNodes([
    { name: 'Page 1', code: 'PAGE-001', type: 'PAGE' },
    { name: 'Page 2', code: 'PAGE-002', type: 'PAGE' }
], 'PARENT-CODE');
```

## 📖 Documentation

Full documentation is available at [\[https://n](https://github.com/AZIRARM/nodify/wiki)](https://github.com/AZIRARM/nodify/wiki)

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
