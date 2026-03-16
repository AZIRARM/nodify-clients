# Nodify Python Client

**Official Python client for Nodify Headless CMS**

[![PyPI version](https://img.shields.io/pypi/v/nodify-python-client.svg)](https://pypi.org/project/nodify-python-client/)
[![Python versions](https://img.shields.io/pypi/pyversions/nodify-python-client.svg)](https://pypi.org/project/nodify-python-client/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A powerful asynchronous Python client built with `aiohttp` to interact with your Nodify instance, enabling you to manage content, nodes, translations, and more programmatically.

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

### From PyPI

```bash
pip install nodify-python-client
```

### From source (development)

```bash
git clone https://github.com/AZIRARM/nodify-clients.git
cd nodify-clients/python
pip install -e .
```

## 🚀 Quick Start

Here's a simple example to get you started:

```python
import asyncio
import uuid
from nodify_client import ReactiveNodifyClient, Node, StatusEnum

async def quick_start():
    # 1. Create a client instance pointing to your Nodify Core instance
    client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .with_base_url("http://localhost:7804")  # URL of your nodify-core instance
            .build()
    )

    try:
        # 2. Authenticate
        auth = await client.login("admin", "Admin123")
        print("✅ Login successful")

        # 3. Create a new node (a structural element like a 'Page' or 'Site')
        node = Node(
            name="My First Page",
            code=f"MY_FIRST_PAGE_{str(uuid.uuid4())[:8].upper()}",
            slug="my-first-page",
            type="PAGE",
            status=StatusEnum.SNAPSHOT,
            default_language="en",
            environment_code="production"
        )

        saved_node = await client.save_node(node)
        print(f"✅ Node created with code: {saved_node.code}")

        # 4. Find all nodes
        all_nodes = await client.find_all_nodes()
        print(f"📊 Total nodes: {len(all_nodes)}")

    except Exception as e:
        print(f"❌ An error occurred: {e}")
    finally:
        # 5. Close the client's HTTP connections
        await client.close()

if __name__ == "__main__":
    asyncio.run(quick_start())
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

```python
import asyncio
import uuid
from nodify_client import (
    ReactiveNodifyClient,
    Node,
    ContentNode,
    Translation,
    Value,
    ContentTypeEnum,
    StatusEnum
)

async def create_complete_scenario():
    # 1. Create a client instance
    client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .with_base_url("http://localhost:7804")
            .build()
    )

    try:
        # 2. Login
        await client.login("admin", "Admin123")
        print("✅ Login successful")

        # 3. Create a parent node (e.g., your website)
        parent_node = Node(
            name="My Multilingual Site",
            code=f"MY_SITE_{str(uuid.uuid4())[:8].upper()}",
            slug="my-site",
            type="SITE",
            status=StatusEnum.SNAPSHOT,
            default_language="en",
            environment_code="production"
        )
        saved_parent = await client.save_node(parent_node)
        print(f"✅ Parent node created: {saved_parent.name}")

        # 4. Create a child page node under the website
        child_node = Node(
            parent_code=saved_parent.code,
            name="Welcome Page",
            code=f"WELCOME_PAGE_{str(uuid.uuid4())[:8].upper()}",
            slug="welcome",
            type="PAGE",
            status=StatusEnum.SNAPSHOT,
            default_language="en",
            environment_code="production"
        )
        saved_child = await client.save_node(child_node)
        print(f"✅ Child node created: {saved_child.name}")

        # 5. Create HTML content with translations and values
        translations = [
            Translation(key="GREETING", language="en", value="Hello"),
            Translation(key="GREETING", language="fr", value="Bonjour"),
            Translation(key="GREETING", language="es", value="Hola"),
        ]

        values = [
            Value(key="USER_NAME", value="Python Developer")
        ]

        content_node = ContentNode(
            parent_code=saved_child.code,
            type=ContentTypeEnum.HTML,
            title="Dynamic Welcome",
            code=f"WELCOME_HTML_{str(uuid.uuid4())[:8].upper()}",
            slug="welcome-html",
            language="en",
            status=StatusEnum.SNAPSHOT,
            environment_code="production",
            content="<h1>$translate(GREETING), $value(USER_NAME)!</h1>\n<p>Welcome to your Nodify site built with Python.</p>",
            translations=translations,
            values=values
        )

        saved_content = await client.save_content_node(content_node)
        print(f"✅ Content node created: {saved_content.title}")

        # 6. Publish the content
        await client.publish_content_node(saved_content.code, True)
        print("✅ Content published")

        # 7. Publish the parent node
        await client.publish_node(saved_parent.code)
        print("✅ Parent node published")

        # 8. Display final information
        print("\n" + "="*60)
        print("🎯 FINAL SCENARIO SUMMARY")
        print("="*60)
        
        print(f"\n📁 PARENT NODE: {saved_parent.name} ({saved_parent.code})")
        print(f"📄 CHILD NODE: {saved_child.name} ({saved_child.code})")
        print(f"📝 CONTENT NODE: {saved_content.title} ({saved_content.code})")
        
        print("\n🌍 TRANSLATIONS:")
        for t in saved_content.translations:
            print(f"   - {t.key} [{t.language}]: {t.value}")
        
        print("\n🔢 VALUES:")
        for v in saved_content.values:
            print(f"   - {v.key}: {v.value}")

    except Exception as e:
        print(f"❌ An error occurred: {e}")
    finally:
        await client.close()

if __name__ == "__main__":
    asyncio.run(create_complete_scenario())
```

## 📖 API Reference

The client provides methods for all Nodify API endpoints, organized by resource:

### Authentication
* `async login(email: str, password: str) -> AuthResponse`
* `async logout() -> None`

### Nodes
* `async find_all_nodes() -> List[Node]`
* `async save_node(node: Node) -> Node`
* `async find_node_by_code(code: str) -> Node`
* `async find_node_by_code_and_status(code: str, status: str) -> Node`
* `async find_nodes_by_parent_code(code: str) -> List[Node]`
* `async publish_node(code: str) -> Node`
* `async delete_node(code: str) -> bool`

### Content Nodes
* `async find_all_content_nodes() -> List[ContentNode]`
* `async save_content_node(content_node: ContentNode) -> ContentNode`
* `async find_content_node_by_code_and_status(code: str, status: str) -> ContentNode`
* `async find_content_nodes_by_node_code(code: str) -> List[ContentNode]`
* `async publish_content_node(code: str, publish: bool) -> ContentNode`
* `async delete_content_node(code: str) -> bool`

### Users
* `async find_all_users() -> List[UserPost]`
* `async save_user(user: UserPost) -> UserPost`
* `async find_user_by_email(email: str) -> UserPost`
* `async change_password(password_data: UserPassword) -> bool`
* `async delete_user(user_id: str) -> bool`

### Plugins
* `async find_not_deleted_plugins() -> List[Plugin]`
* `async save_plugin(plugin: Plugin) -> Plugin`
* `async enable_plugin(plugin_id: str) -> Plugin`
* `async disable_plugin(plugin_id: str) -> Plugin`
* `async export_plugin(plugin_id: str) -> bytes`

### Feedback
* `async find_all_feedback() -> List[Feedback]`
* `async save_feedback(feedback: Feedback) -> Feedback`
* `async get_content_charts() -> List[FeedbackCharts]`

### Locks
* `async acquire_lock(code: str) -> bool`
* `async release_lock(code: str) -> bool`
* `async get_lock_owner(code: str) -> LockInfo`
* `async get_all_locks() -> List[LockInfo]`

### Data
* `async save_data(data: Data) -> Data`
* `async find_data_by_key(key: str) -> Data`
* `async find_data_by_content_code(code: str, params: PaginationParams = None) -> List[Data]`

## ⚙️ Configuration

### Client Builder Options

```python
client = ReactiveNodifyClient.create(
    ReactiveNodifyClient.builder()
        .with_base_url("https://your-nodify-instance.com")
        .with_timeout(30000)                    # Timeout in milliseconds
        .with_auth_token("your-jwt-token")       # Optional: pre-authenticated token
        .with_header("X-Custom-Header", "value") # Custom headers
        .build()
)
```

### Pagination

```python
from nodify_client import PaginationParams

params = PaginationParams(current_page=1, limit=20)  # Page 1, 20 items per page
data = await client.find_data_by_content_code("CONTENT_CODE", params)
```

## 📋 Requirements

- Python 3.8 or higher
- aiohttp >= 3.8.0

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## 📄 License

This client library is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.