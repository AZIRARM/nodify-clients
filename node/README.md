# @azirarm/nodify-node-client

**Official Node.js client for Nodify Headless CMS**

[![npm version](https://img.shields.io/npm/v/nodify-node-client.svg)](https://www.npmjs.com/package/nodify-node-client)
[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)

A powerful and flexible TypeScript client to interact with your Nodify instance, enabling you to manage content, nodes, translations, and more programmatically.

## 📋 Prerequisites

Before using this client, you need access to a **Nodify Headless CMS** instance. Nodify is a powerful, multilingual headless CMS that delivers content through APIs to any channel (websites, mobile apps, IoT, etc.).

### Option 1: Use the Public Demo (For Testing)

You can test the client against the public Nodify demo instance:
*   **Demo URL the Studio (for data analysts, maketers, ...) :** [Nodify Demo](https://nodify.azirar.ovh) (Check repository for availability)
*   **Demo URL (for developpers):** [Nodify Demo](https://nodify-core.azirar.ovh) (Check repository for availability)
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

**Explanation of Variables:**
*   `MONGO_URL`: Connection string for your MongoDB database (e.g., `mongodb://mongo:27017/nodify`).
*   `REDIS_URL`: Connection string for your Redis instance (e.g., `redis://redis:6379`).
*   `ADMIN_PWD`: The password you will use to log in as the `admin` user.
*   `API_URL`: The internal URL for the `nodify-api` service (used by `nodify-core`).
*   `CORE_URL` / `API_URL` (in `nodify-ui`): The URLs for the UI to connect to the backend services.

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
      - "27017:27017" # Expose Mongo port if needed

  redis:
    image: redis:latest
    ports:
      - "6379:6379"   # Expose Redis port if needed

  nodify-core:
    image: azirar/nodify-core:latest
    depends_on:
      - mongo
      - redis
    environment:
      MONGO_URL: "mongodb://mongo:27017/nodify"
      ADMIN_PWD: "Admin123" # Default password, change in production!
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

After running either Docker setup, you can access the Nodify UI at `http://localhost:7821` (or the port you mapped for the UI). The backend services (`nodify-core` and `nodify-api`) will be available at the ports you mapped (e.g., `7804` and `7805`).

## 📦 Installation

Install the client package via npm:

```bash
npm install @azirarm/nodify-node-client
```

## 🚀 Quick Start

Here's a simple example to get you started:

```typescript
import { NodifyClient, Node, NodeStatus } from '@azirarm/nodify-node-client';

async function main() {
  // 1. Create a client instance pointing to your Nodify Core instance
  const client = new NodifyClient({
    baseUrl: 'http://localhost:7804', // URL of your nodify-core instance
  });

  try {
    // 2. Authenticate
    await client.login('admin', 'YourAdminPassword');
    console.log('✅ Login successful');

    // 3. Create a new node (a structural element like a 'Page' or 'Site')
    const newNode: Node = {
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
    // 5. Close the client's HTTP connections
    await client.close();
  }
}

main();
```

## ✨ Core Concepts

*   **Node**: Represents a structural element in your content hierarchy (e.g., a `SITE`, a `PAGE`, a `FOLDER`). Nodes can have child nodes and content nodes attached.
*   **Content Node**: Represents the actual content itself. It can be of various types (`HTML`, `JSON`, `DATA`, `FILE`, etc.). It's where your translations and dynamic values live.
*   **Dynamic Directives**: Nodify supports powerful directives within your content strings:
    *   `$translate(KEY)`: Automatically replaces the directive with the translated value for the current language based on translations attached to the content node.
    *   `$value(KEY)`: Replaces the directive with a dynamic value stored in the content node's `values` array.

## 📝 Advanced Example: Creating Multilingual Content

This example demonstrates creating a parent node, a child page, and an HTML content node with translations and a dynamic value.

```typescript
import { NodifyClient, Node, ContentNode, ContentType, NodeStatus, Translation, Value } from '@azirarm/nodify-node-client';

async function createCompleteScenario() {
  const client = new NodifyClient({ baseUrl: 'http://localhost:7804' });
  await client.login('admin', 'YourAdminPassword');

  // 1. Create a parent node (e.g., your website)
  const websiteNode: Node = {
    name: 'My Multilingual Site',
    code: 'MY_SITE',
    slug: 'my-site',
    type: 'SITE',
    status: NodeStatus.SNAPSHOT,
    defaultLanguage: 'en',
  };
  const savedSite = await client.saveNode(websiteNode);

  // 2. Create a child page node under the website
  const pageNode: Node = {
    parentCode: savedSite.code,
    name: 'Welcome Page',
    code: 'WELCOME_PAGE',
    slug: 'welcome',
    type: 'PAGE',
    status: NodeStatus.SNAPSHOT,
    defaultLanguage: 'en',
  };
  const savedPage = await client.saveNode(pageNode);

  // 3. Create HTML content attached to the page, with translations and a value
  const translations: Translation[] = [
    { key: 'GREETING', language: 'en', value: 'Hello' },
    { key: 'GREETING', language: 'fr', value: 'Bonjour' },
    { key: 'GREETING', language: 'es', value: 'Hola' },
  ];

  const values: Value[] = [
    { key: 'USER_NAME', value: 'Nodify Developer' },
  ];

  const htmlContent: ContentNode = {
    parentCode: savedPage.code,
    type: ContentType.HTML,
    title: 'Dynamic Welcome',
    code: 'WELCOME_HTML',
    slug: 'welcome-html',
    language: 'en',
    status: NodeStatus.SNAPSHOT,
    content: '<h1>$translate(GREETING), $value(USER_NAME)!</h1><p>Welcome to your Nodify site.</p>',
    translations: translations,
    values: values,
  };

  const savedContent = await client.saveContentNode(htmlContent);
  console.log(`✅ Content node created with code: ${savedContent.code}`);

  // 4. Publish the content to make it live
  await client.publishContentNode(savedContent.code, true);
  console.log('✅ Content published');

  await client.close();
}

createCompleteScenario();
```

## 📖 API Reference

The client provides methods for all Nodify API endpoints, grouped by resource:

*   **Authentication:** `login()`, `logout()`
*   **Nodes:** `findAllNodes()`, `saveNode()`, `findNodeByCode()`, `publishNode()`, `deleteNode()`, and many more for managing your content structure.
*   **Content Nodes:** `findAllContentNodes()`, `saveContentNode()`, `findContentNodeByCodeAndStatus()`, `publishContentNode()`, `deleteContentNode()`.
*   **Users:** `findAllUsers()`, `saveUser()`, `changePassword()`.
*   **Plugins:** `findAllPlugins()`, `savePlugin()`, `enablePlugin()`.
*   **Feedback:** `findAllFeedback()`, `saveFeedback()`, `getContentCharts()`.
*   **Data:** `saveData()`, `findDataByKey()`, `findDataByContentCode()`.
*   **Locks:** `acquireLock()`, `releaseLock()`, `getLockOwner()`.

Refer to the source code or TypeScript definitions for the complete list of available methods and their signatures.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## 📄 License

This client library is licensed under the **Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)** license, consistent with the Nodify CMS itself.

This means you are free to **share** and **adapt** the software for **non-commercial purposes**, as long as you provide appropriate attribution. You may not use it for commercial purposes.

For the full license text, please visit: [https://creativecommons.org/licenses/by-nc/4.0/](https://creativecommons.org/licenses/by-nc/4.0/)
