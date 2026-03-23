# Nodify Java Examples - Complete Guide

A comprehensive collection of Java examples demonstrating how to build different types of applications using the Nodify Java client. These examples showcase the power and flexibility of Nodify Headless CMS for building dynamic, multilingual content-driven applications.

## 📋 Table of Contents

- [Overview](#overview)
- [Examples Included](#examples-included)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Project Structure](#project-structure)
- [Example 1: Tiny Tales - Children's Stories](#example-1-tiny-tales---childrens-stories)
- [Example 2: Dev Community Forum](#example-2-dev-community-forum)
- [Example 3: Tech Blog](#example-3-tech-blog)
- [Common Features](#common-features)
- [How It Works](#how-it-works)
- [API Endpoints](#api-endpoints)
- [Customization](#customization)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## 🎯 Overview

This repository contains three complete Java examples built with the Nodify Java client:

| Example | Description | Key Features |
|---------|-------------|--------------|
| **Tiny Tales** | Children's stories platform | Story cards, multilingual stories, dynamic loading |
| **Dev Forum** | Developer community forum | Topics, replies, message threads, language badges |
| **Tech Blog** | Technical blog with code examples | Blog posts, syntax highlighting, multilingual content |

Each example demonstrates different content types, data structures, and Nodify capabilities while maintaining a consistent architecture.

## ✨ Features Common to All Examples

- **Reactive Programming**: Built with Project Reactor for non-blocking operations
- **Multilingual Support**: EN, FR, ES translations using `$translate()` directives
- **Dynamic Content Loading**: JavaScript fetches content based on language selection
- **Hierarchical Structure**: Organized nodes for different content types
- **Complete Publishing Workflow**: Create, translate, and publish content nodes
- **Responsive Design**: Mobile-friendly interfaces with modern CSS
- **Type Safety**: Full Java type safety with custom models

## 📋 Prerequisites

Before running these examples, you need:

1. **Java 17 or higher** installed (JDK 17+ required)
2. **Maven 3.6+** (for dependency management)
3. **Nodify CMS Instance** running:
    - Core URL: `http://localhost:8080` (or your instance)
    - API URL: `http://localhost:1080` (or your instance)
4. **Default credentials**: `admin` / `Admin13579++`

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/AZIRARM/nodify-clients.git
cd examples/java-examples
```

### 2. Build with Maven

```bash
mvn clean compile
```

### 3. Configure Connection (if needed)

Update the URLs in each example file:

```java
private static final String BASE_URL = "http://localhost:8080";  // Nodify Core URL
private static final String API_URL = "http://localhost:1080";   // Nodify API URL
```

### 4. Run an Example

```bash
# Run Tiny Tales example
mvn exec:java -Dexec.mainClass="com.tinytales.example.TinyTalesExample"

# Run Dev Forum example
mvn exec:java -Dexec.mainClass="com.forum.example.NodifyForumExample"

# Run Tech Blog example
mvn exec:java -Dexec.mainClass="com.blog.example.NodifyBlogExample"
```

## 📁 Project Structure

```
java-examples/
├── pom.xml                           # Maven configuration (Java 17+)
├── src/main/java/
│   ├── com.tinytales.example/        # Tiny Tales Example
│   │   ├── TinyTalesExample.java     # Main application
│   │   ├── models/
│   │   │   ├── Author.java           # Author model
│   │   │   └── Story.java            # Story model
│   │   └── services/
│   │       └── TinyTalesService.java # Service layer
│   ├── com.forum.example/            # Dev Forum Example
│   │   ├── NodifyForumExample.java   # Main application
│   │   └── services/
│   │       └── ForumService.java     # Forum service
│   └── com.blog.example/             # Tech Blog Example
│       ├── NodifyBlogExample.java    # Main application
│       ├── models/
│       │   ├── Author.java           # Author model
│       │   └── BlogPost.java         # Blog post model
│       └── services/
│           └── BlogService.java      # Blog service
```

## 📖 Example 1: Tiny Tales - Children's Stories

A magical platform for children's stories with multilingual support and dynamic story cards.

### Content Structure Created

```
📁 Site Node (TINY-TALES-SITE-XXXXXX)
├── 🎨 Style Node (STYLE-XXXXXX)           # CSS styles
├── 📜 Script Node (SCRIPT-XXXXXX)         # JavaScript for dynamic loading
├── 📄 Main Page (HTML-XXXXXX)             # Landing page
└── 📁 Stories Node (STORIES-NODE-XXXXXX)
    ├── 📖 Story 1: The Little Fox (JSON)
    └── 📖 Story 2: The Sleepy Bear (JSON)
```

### Features

- **Story Cards**: Two beautifully designed stories with animal icons
- **Language Switcher**: Toggle between English and French
- **Dynamic Loading**: Stories fetched and rendered via JavaScript
- **Custom Fonts**: Comic-inspired typography for children
- **Responsive Grid**: Two-column layout for desktop, single column on mobile

### Running Tiny Tales

```bash
mvn exec:java -Dexec.mainClass="com.tinytales.example.TinyTalesExample"
```

### Access URLs

After successful execution, you'll see:

```
📊 Final Results:
  📌 Site node code: TINY-TALES-SITE-ABC12345
  📌 Stories node code: STORIES-NODE-DEF67890
  📌 Main page code: PAGE-GHI12345

🔍 Access URLs:
  Main page: http://localhost:8080/v0/content-node/code/PAGE-GHI12345
```

### Story Preview

- **The Little Fox Who Lost His Spots**: A heartwarming tale about self-discovery
- **The Sleepy Bear and the Honey Moon**: A whimsical adventure about dreams

## 💬 Example 2: Dev Community Forum

A developer discussion forum where users can create topics and reply with messages.

### Content Structure Created

```
📁 Site Node (DEV-FORUM-SITE-XXXXXX)
├── 🎨 Style Node (STYLE-XXXXXX)           # Modern dark theme CSS
├── 📜 Script Node (SCRIPT-XXXXXX)         # Forum JavaScript logic
├── 📄 Main Page (HTML-XXXXXX)             # Forum interface
└── 📁 Topics Node (TOPICS-NODE-XXXXXX)
    ├── 💬 Java - Spring Boot Best Practices (JSON)
    ├── 💬 Angular - Signals vs RxJS (JSON)
    ├── 💬 Python - FastAPI vs Django (JSON)
    └── 💬 PHP - Laravel 11 Features (JSON)
```

### Features

- **Topic Listing**: View all programming language topics in a table
- **Reply System**: Users can add replies to any topic
- **Message Count**: Real-time reply counts per topic
- **Language Badges**: Visual badges for different programming languages
- **Dark Theme**: Modern, developer-friendly dark interface
- **Threaded Discussions**: View topics and replies in a clean layout

### Running Dev Forum

```bash
mvn exec:java -Dexec.mainClass="com.forum.example.NodifyForumExample"
```

### Access URLs

```
📊 Dev Forum Main Page URL:
  http://localhost:8080/v0/content-node/code/DEV-FORUM-MAIN-XXXXXX
```

### Forum Features in Action

| Feature | Description |
|---------|-------------|
| View Topics | Click on any topic title to view full content |
| Add Replies | Post comments using the reply form |
| Reply Count | See how many replies each topic has |
| Author Info | Displays who created each topic |
| Language Badges | Java (☕), Angular (🅰), Python (🐍), PHP (🐘) |

## 📝 Example 3: Tech Blog

A technical blog with code examples and multilingual support, perfect for developers sharing knowledge.

### Content Structure Created

```
📁 Site Node (BLOG-SITE-XXXXXX)
├── 🎨 Style Node (STYLE-XXXXXX)           # Blog CSS styles
├── 📜 Script Node (SCRIPT-XXXXXX)         # Dynamic post loading
├── 📄 Main Page (HTML-XXXXXX)             # Blog homepage
├── 📝 Blog Post 1: Presentation (JSON)    # Introduction post
└── 📝 Blog Post 2: Java Client (JSON)     # Technical post with code
```

### Features

- **Dynamic Post Loading**: JavaScript fetches and renders all blog posts
- **Code Syntax Highlighting**: Java code examples with proper formatting
- **Rich Text Support**: HTML content with `<pre><code>` blocks
- **Multilingual Posts**: Content available in EN, FR, ES
- **Author Attribution**: Each post includes author information
- **Clean Blog Layout**: Two-column grid with modern card design

### Running Tech Blog

```bash
mvn exec:java -Dexec.mainClass="com.blog.example.NodifyBlogExample"
```

### Access URLs

```
📊 Final Results:
  📌 Style content code: BLOG-STYLES-XXXXXX
  📌 Script content code: BLOG-SCRIPT-XXXXXX
  📌 Post 1 code: POST-XXXXXX
  📌 Post 2 code: POST-YYYYYY
  📌 Main page code: BLOG-MAIN-XXXXXX

🔍 Access URLs:
  Main page: http://localhost:8080/v0/content-node/code/BLOG-MAIN-XXXXXX
  Post 1: http://localhost:1080/contents/code/POST-XXXXXX?payloadOnly=true
  Post 2: http://localhost:1080/contents/code/POST-YYYYYY?payloadOnly=true
```

### Blog Post Examples

**Post 1 - Presentation**: Introduces Nodify Headless CMS with simple examples
**Post 2 - Java Client Tutorial**: Shows how to connect and use the Nodify Java client with actual code examples

## 🔧 Common Features

### Translation Mechanism

All examples use the same translation system with `$translate(KEY)` directives:

```json
{
  "title": "$translate(TITLE)",
  "description": "$translate(DESCRIPTION)"
}
```

Translations are stored in each content node:

```java
service.addTranslationKey(contentCode, "en", "TITLE", "English Title")
       .then(service.addTranslationKey(contentCode, "fr", "TITLE", "French Title"))
       .then(service.addTranslationKey(contentCode, "es", "TITLE", "Spanish Title"));
```

### Language Switching

When users select a language:
1. `changeLanguage(lang)` is called
2. URL is updated with `?translation=XX`
3. Page reloads
4. All content is fetched with the translation parameter
5. `$translate()` directives are resolved by the API

### Content Loading Pattern

All examples follow a similar pattern:

1. **Authenticate** with Nodify
2. **Create Site Node** (parent container)
3. **Create Folder Nodes** (for organization)
4. **Create Content Nodes** (style, script, JSON data)
5. **Add Translations** to all content
6. **Publish** all nodes and content
7. **Display URLs** for access

## 🔌 API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/authentication/login` | POST | Authenticate and get JWT token |
| `/v0/nodes/` | POST | Create nodes (site, folders) |
| `/v0/content-node/` | POST | Create content nodes (style, script, HTML, JSON) |
| `/v0/nodes/code/{code}/publish` | POST | Publish nodes |
| `/v0/content-node/code/{code}/publish` | POST | Publish content nodes |
| `/v0/datas/` | POST | Add data objects (forum replies) |
| `/v0/nodes/parent/{code}` | GET | Get child nodes |
| `/contents/node/code/{code}?status=PUBLISHED` | GET | Get content metadata |
| `/contents/code/{code}?payloadOnly=true&translation={lang}` | GET | Get translated content |
| `/datas/contentCode/{code}` | GET | Get data objects (replies) |

## 🎨 Customization

### Adding More Stories (Tiny Tales)

Add new stories to the `storiesNode`:

```java
Story newStory = new Story(
    null,
    "New Story Title",
    "{\"title\": \"$translate(TITLE)\", \"content\": \"$translate(CONTENT)\"}",
    "Description",
    author,
    List.of("new", "tags"),
    "new-story-slug",
    "en",
    ContentTypeEnum.JSON,
    Instant.now(),
    Instant.now(),
    false
);
```

Add translations:

```java
service.addTranslationKey(newStoryCode, "en", "TITLE", "English Title")
       .then(service.addTranslationKey(newStoryCode, "en", "CONTENT", "English content..."))
       .then(service.addTranslationKey(newStoryCode, "fr", "TITLE", "Titre Français"));
```

### Adding New Topics (Dev Forum)

Add topics in the `createSampleTopics` method:

```java
forumService.createTopic(
    "🚀 Rust - Async Programming Patterns",
    "Share your experiences with async/await in Rust",
    "Rustacean"
);
```

### Adding Blog Posts (Tech Blog)

Create new blog posts with JSON content:

```java
String jsonContent = """
    {
      "title": "$translate(TITLE)",
      "description": "<p>Your HTML content here</p>"
    }
    """;

BlogPost newPost = new BlogPost(
    null,
    "Post Title",
    jsonContent,
    "Post description",
    author,
    List.of("tags"),
    "post-slug",
    "en",
    ContentTypeEnum.JSON,
    Instant.now(),
    Instant.now(),
    false
);
```

### Modifying Styles

Edit the CSS in each example's `createStyleContent()` method to customize colors, fonts, and layouts.

## 🐛 Troubleshooting

### Common Issues

**Issue**: Java version error
- **Solution**: Ensure Java 17+ is installed: `java -version`

**Issue**: Connection refused
- **Solution**: Verify Nodify instance is running on `http://localhost:8080` and `http://localhost:1080`

**Issue**: Login failed
- **Solution**: Check credentials are `admin` / `Admin13579++`

**Issue**: Maven build fails
- **Solution**: Run `mvn clean install` to download dependencies

**Issue**: Content not displaying
- **Solution**:
    - Check browser console for JavaScript errors
    - Verify all nodes were published successfully
    - Ensure translation parameter is being passed correctly

**Issue**: Forum replies not saving
- **Solution**: Check that the Data API endpoint is accessible and permissions are set

### Debugging Tips

1. **Enable Detailed Logging**: Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.16</version>
</dependency>
```

2. **Check API Responses**: Open browser developer tools (F12) → Network tab

3. **Verify Node Creation**: Check the console output for node codes and URLs

4. **Test Direct API Access**: Use curl to verify content is accessible:
```bash
curl "http://localhost:8080/v0/content-node/code/PAGE-XXXXXX"
```

## 📄 License

These examples are provided under the MIT License. The Nodify Java client and Nodify CMS have their own licenses.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on [GitHub](https://github.com/AZIRARM/nodify-clients).

## 📚 Related Resources

- [Nodify Java Client Documentation](https://github.com/AZIRARM/nodify-clients/tree/main/java)
- [Nodify Node.js Client](https://github.com/AZIRARM/nodify-clients/tree/main/node)
- [Nodify PHP Client](https://github.com/AZIRARM/nodify-clients/tree/main/php)
- [Nodify Python Client](https://github.com/AZIRARM/nodify-clients/tree/main/python)
- [Nodify Headless CMS](https://nodify.azirar.ovh)

---

Built with ❤️ using [Nodify Headless CMS](https://nodify.azirar.ovh) and Java 17+