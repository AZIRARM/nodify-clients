# nodify-node-client

**Official Node.js client for Nodify Headless CMS**

[![npm version](https://img.shields.io/npm/v/nodify-node-client.svg)](https://www.npmjs.com/package/nodify-node-client)
[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)

A powerful and flexible TypeScript client to interact with your Nodify instance, enabling you to manage content, nodes, translations, and more programmatically.

## 📋 Prerequisites

Before using this client, you need access to a **Nodify Headless CMS** instance. Nodify is a powerful, multilingual headless CMS that delivers content through APIs to any channel (websites, mobile apps, IoT, etc.).

### Option 1: Use the Public Demo (For Testing)

You can test the client against the public Nodify demo instance:
- **Studio URL (for content editors):** [Nodify Demo](https://nodify.azirar.ovh)
- **API URL (for developers):** [Nodify Core](https://nodify-core.azirar.ovh)
- **Credentials:**
  - Username: `admin`
  - Password: `Admin13579++`

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
      MONGO_URL: "mongodb://your-mongo-host:27017/nodify"
      ADMIN_PWD: "YourAdminPassword"
      API_URL: "http://nodify-api:1080"
      TZ: "${TZ:-Europe/Paris}"
      REDIS_URL: "redis://your-redis-host:6379"
      JAVA_OPTS: "-Xmx768m -Xms384m"
    ports:
      - "7804:8080"

  nodify-api:
    image: azirar/nodify-api:latest
    environment:
      MONGO_URL: "mongodb://your-mongo-host:27017/nodify"
      TZ: "${TZ:-Europe/Paris}"
      REDIS_URL: "redis://your-redis-host:6379"
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

After running either Docker setup, you can access the Nodify UI at `http://localhost:7821`. The backend services are available at:
- `http://localhost:7804` (nodify-core)
- `http://localhost:7805` (nodify-api)

## 📦 Installation

Install the client package via npm:

```bash
npm install nodify-node-client
```

## 🚀 Quick Start

Here's a simple example to get you started using the builder pattern:

```typescript
import { NodifyClient, NodeStatus } from 'nodify-node-client';

async function main() {
  // 1. Create a client instance using the builder pattern
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:7804')
    .withTimeout(30000)
    .build();

  try {
    // 2. Authenticate
    await client.login('admin', 'YourAdminPassword');
    console.log('✅ Login successful');

    // 3. Create a new node (a structural element like a 'Page' or 'Site')
    const newNode = {
      name: 'My First Page',
      code: 'MY_FIRST_PAGE',
      slug: 'my-first-page',
      type: 'PAGE',
      status: NodeStatus.SNAPSHOT,
      defaultLanguage: 'en',
    };

    const savedNode = await client.saveNode(newNode);
    console.log(`✅ Node created with code: ${savedNode.code}`);

    // 4. Find all nodes
    const allNodes = await client.findAllNodes();
    console.log(`📊 Total nodes: ${allNodes.length}`);

  } catch (error) {
    console.error('❌ An error occurred:', error);
  } finally {
    // 5. Close the client
    await client.close();
  }
}

main();
```

## 🔧 Builder Pattern Configuration

The client uses a builder pattern for flexible configuration:

```typescript
const client = NodifyClient.builder()
  .withBaseUrl('https://nodify-core.azirar.ovh')  // Required
  .withTimeout(30000)                               // Optional, default: 30000
  .withAuthToken('your-jwt-token')                 // Optional, if you already have a token
  .withHeader('X-Custom-Header', 'value')          // Optional, add custom headers
  .withDefaultHeaders({                            // Optional, set multiple headers
    'X-API-Key': 'your-api-key',
    'X-Client-Version': '1.0.0'
  })
  .withAuthErrorHandler(async () => {               // Optional, handle token refresh
    const newToken = await refreshToken();
    return newToken;
  })
  .build();
```

## ✨ Core Concepts

- **Node**: Represents a structural element in your content hierarchy (e.g., a `SITE`, a `PAGE`, a `FOLDER`). Nodes can have child nodes and content nodes attached.
- **Content Node**: Represents the actual content itself. It can be of various types (`HTML`, `JSON`, `PICTURE`, `STYLE`, `SCRIPT`, etc.). It's where your translations and dynamic values live.
- **Content Types**: Available content types:
    - `HTML` - HTML content
    - `JSON` - JSON data
    - `PICTURE` - Image content
    - `STYLE` - CSS styles
    - `SCRIPT` - JavaScript code
    - `FILE` - Binary files
    - `XML` - XML content
- **Node Status**: Content lifecycle states:
    - `SNAPSHOT` - Draft/working version
    - `PUBLISHED` - Live/production version
    - `ARCHIVE` - Archived content
    - `DELETED` - Soft-deleted content
- **Dynamic Directives**: Nodify supports powerful directives within your content strings:
    - `$translate(KEY)`: Replaces with translated value for current language
    - `$value(KEY)`: Replaces with dynamic value from content node's values array
    - `$content(CODE)`: Embeds another content node (useful for including styles/scripts)

## 📝 Advanced Examples

### Creating a Complete Website Structure

```typescript
import { 
  NodifyClient, 
  NodeStatus, 
  ContentNodeType, 
  ContentNode 
} from 'nodify-node-client';

async function createWebsite() {
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:7804')
    .build();

  await client.login('admin', 'YourAdminPassword');

  // 1. Create a site node
  const site = await client.saveNode({
    name: 'My Awesome Site',
    code: 'MY_SITE',
    slug: 'my-site',
    type: 'SITE',
    status: NodeStatus.SNAPSHOT,
    defaultLanguage: 'en',
    languages: ['en', 'fr', 'es'],
    description: 'My personal website'
  });

  // 2. Create a page under the site
  const page = await client.saveNode({
    parentCode: site.code,
    name: 'About Us',
    code: 'ABOUT_PAGE',
    slug: 'about',
    type: 'PAGE',
    status: NodeStatus.SNAPSHOT
  });

  // 3. Create HTML content for the page
  const content: ContentNode = {
    parentCode: page.code,
    type: ContentNodeType.HTML,
    title: 'About Us Page',
    code: 'ABOUT_HTML',
    slug: 'about-us',
    language: 'en',
    status: NodeStatus.SNAPSHOT,
    content: '<h1>About Us</h1><p>Welcome to our website!</p>'
  };

  const savedContent = await client.saveContentNode(content);
  
  // 4. Publish the content
  await client.publishContentNode(savedContent.code!, true);
  
  console.log('✅ Website created successfully!');
  await client.close();
}
```

### Creating Multilingual Content with Translations

```typescript
import { 
  NodifyClient, 
  NodeStatus, 
  ContentNodeType,
  ContentNode,
  Translation,
  Value
} from 'nodify-node-client';

async function createMultilingualContent() {
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:7804')
    .build();

  await client.login('admin', 'YourAdminPassword');

  // Create translations
  const translations: Translation[] = [
    { key: 'GREETING', language: 'en', value: 'Hello' },
    { key: 'GREETING', language: 'fr', value: 'Bonjour' },
    { key: 'GREETING', language: 'es', value: 'Hola' },
    { key: 'WELCOME', language: 'en', value: 'Welcome to our site!' },
    { key: 'WELCOME', language: 'fr', value: 'Bienvenue sur notre site!' },
    { key: 'WELCOME', language: 'es', value: '¡Bienvenido a nuestro sitio!' }
  ];

  // Create dynamic values
  const values: Value[] = [
    { key: 'USER_NAME', value: 'Guest' },
    { key: 'SITE_NAME', value: 'My Awesome Site' }
  ];

  // Create content with translations and values
  const content: ContentNode = {
    parentCode: 'MY_SITE',
    type: ContentNodeType.HTML,
    title: 'Welcome Page',
    code: 'WELCOME_PAGE',
    slug: 'welcome',
    language: 'en',
    status: NodeStatus.SNAPSHOT,
    content: '<h1>$translate(GREETING), $value(USER_NAME)!</h1><p>$translate(WELCOME)</p><p>Welcome to $value(SITE_NAME)</p>',
    translations: translations,
    values: values
  };

  const savedContent = await client.saveContentNode(content);
  await client.publishContentNode(savedContent.code!, true);
  
  console.log('✅ Multilingual content created!');
  await client.close();
}
```

### Working with Images and Media

```typescript
import { NodifyClient, NodeStatus, ContentNodeType } from 'nodify-node-client';

async function createImageGallery() {
  const client = NodifyClient.builder()
    .withBaseUrl('http://localhost:7804')
    .build();

  await client.login('admin', 'YourAdminPassword');

  const images = [
    { name: 'Photo 1', url: 'https://example.com/photo1.jpg', alt: 'Beautiful landscape' },
    { name: 'Photo 2', url: 'https://example.com/photo2.jpg', alt: 'City skyline' }
  ];

  for (const img of images) {
    await client.saveContentNode({
      parentCode: 'GALLERY_NODE',
      type: ContentNodeType.PICTURE,
      title: img.name,
      code: `IMG_${img.name.toUpperCase().replace(/\s+/g, '_')}`,
      slug: img.name.toLowerCase().replace(/\s+/g, '-'),
      language: 'en',
      status: NodeStatus.SNAPSHOT,
      content: img.url,
      values: [
        { key: 'ALT', value: img.alt }
      ]
    });
  }

  console.log('✅ Image gallery created!');
  await client.close();
}
```

## 📖 API Reference

The client provides methods for all Nodify API endpoints, grouped by resource:

### Authentication
- `login(email, password)`: Authenticate and get JWT token
- `logout()`: Clear authentication token
- `getAuthToken()`: Get current auth token
- `setAuthToken(token)`: Manually set auth token

### Nodes
- `findAllNodes()`: Get all nodes
- `saveNode(node)`: Create or update a node
- `findNodeByCodeAndStatus(code, status)`: Find node by code and status
- `findNodesByParentCode(code)`: Find child nodes
- `publishNode(code)`: Publish a node
- `deleteNode(code)`: Delete a node

### Content Nodes
- `findAllContentNodes()`: Get all content nodes
- `saveContentNode(contentNode)`: Create or update content
- `findContentNodeByCodeAndStatus(code, status)`: Find content by code and status
- `publishContentNode(code, publish)`: Publish or unpublish content
- `deleteContentNode(code)`: Delete content
- `fillContent(code, status, payload)`: Fill content with data

### Users & Roles
- `findAllUsers()`: Get all users
- `saveUser(user)`: Create or update user
- `changePassword(passwordData)`: Change user password
- `findAllUserRoles()`: Get all user roles
- `saveUserRole(role)`: Create or update role

### Feedback & Analytics
- `findAllFeedback()`: Get all feedback
- `saveFeedback(feedback)`: Submit feedback
- `getContentCharts()`: Get content analytics charts
- `getContentDisplayCharts()`: Get display analytics

### Data Management
- `saveData(data)`: Save custom data
- `findDataByKey(key)`: Find data by key
- `findDataByContentCode(code, params)`: Find data by content code

### Lock Management
- `acquireLock(code)`: Acquire lock on resource
- `releaseLock(code)`: Release lock
- `getLockOwner(code)`: Get current lock owner
- `getAllLocks()`: Get all active locks

### Utilities
- `health()`: Check API health
- `close()`: Close the client connections

For complete API documentation, refer to the TypeScript definitions in the package or visit the [Nodify API Documentation](https://nodify.azirar.ovh/docs).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on [GitHub](https://github.com/AZIRARM/nodify-clients).

## 📄 License

This client library is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)** license, consistent with the Nodify CMS itself.

This means you are free to **share** and **adapt** the software for **non-commercial purposes**, as long as you provide appropriate attribution. You may not use it for commercial purposes.

For the full license text, please visit: [https://creativecommons.org/licenses/by-nc/4.0/](https://creativecommons.org/licenses/by-nc/4.0/)
