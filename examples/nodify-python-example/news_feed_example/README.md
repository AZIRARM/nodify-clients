# Nodify News Feed Example - Python Client

A complete example of a dynamic multilingual news feed built with the Nodify Python client. This example demonstrates how to create a fully functional news feed with categories, articles, translations, and comments.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Customization](#customization)
- [Troubleshooting](#troubleshooting)
- [License](#license)

## 🎯 Overview

This example creates a dynamic news feed with:

- **Categories**: Technology, Sports, Politics
- **Articles**: Multiple articles per category with full content
- **Multilingual Support**: EN, FR, ES translations using `$translate()` directives
- **Comments**: User comments stored via the Data API
- **Dynamic Filtering**: Filter articles by category
- **Responsive UI**: Modern, mobile-friendly interface

## ✨ Features

- **Multilingual Content**: Articles support EN, FR, ES translations
- **Dynamic Category Filtering**: Click buttons to filter articles by category
- **Article Modal**: Click any article to view full content in a modal window
- **Comment System**: Add and view comments on articles
- **Language Switcher**: Change language on the fly with URL parameter `?translation=XX`
- **Automatic Translation**: `$translate()` directives resolved by the Nodify API
- **Hierarchical Structure**: Clean organization with categories and articles
- **Dynamic Codes**: All node and content codes are generated dynamically

## 📋 Prerequisites

Before running this example, you need:

1. **Python 3.8+** installed
2. **Nodify CMS Instance** running:
   - Core URL: `http://localhost:8080` (or your instance)
   - API URL: `http://localhost:1080` (or your instance)
3. **Default credentials**: `admin` / `Admin13579++`

## 🚀 Installation

### 1. Clone or create the example file

```bash
mkdir nodify-news-feed-example
cd nodify-news-feed-example
```

Create a file `news_feed_example.py` with the code provided above.

### 2. Create and activate a virtual environment

```bash
# Create virtual environment
python -m venv venv

# Activate on Windows (Git Bash)
source venv/Scripts/activate

# Activate on Windows (PowerShell)
.\venv\Scripts\Activate.ps1

# Activate on Linux/Mac
source venv/bin/activate
```

### 3. Install dependencies

```bash
pip install aiohttp
pip instal nodify-python-client
```

Note: The example uses direct HTTP requests, so no additional client library is needed.

### 4. Run the example

```bash
python news_feed_example.py
```

## 📁 Project Structure

```
nodify-news-feed-example/
├── news_feed_example.py    # Main example file
├── README.md               # This file
└── venv/                   # Virtual environment (created after setup)
```

## 🔧 How It Works

### 1. Content Creation

The builder creates the following structure in Nodify:

```
📁 Site Node (SITE-XXXXXX)
├── 🎨 Style Node (STYLE-XXXXXX)           # CSS styles
├── 📜 Script Node (SCRIPT-XXXXXX)         # JavaScript code
├── 📄 Landing Page (PAGE-XXXXXX)          # HTML page
└── 📁 Categories Container (CATEGORIES-XXXXXX)
    ├── 📁 Technology (CAT-XXXXXX)
    │   ├── 📝 POST-XXXXXX (Article 1 with EN/FR/ES translations)
    │   └── 📝 POST-XXXXXX (Article 2 with EN/FR/ES translations)
    ├── 📁 Sports (CAT-XXXXXX)
    │   └── 📝 POST-XXXXXX (Article with EN/FR/ES translations)
    └── 📁 Politics (CAT-XXXXXX)
        └── 📝 POST-XXXXXX (Article with EN/FR/ES translations)
```

### 2. Translation Mechanism

Articles use the `$translate()` directive:

```json
{
  "title": "$translate(title)",
  "summary": "$translate(summary)",
  "content": "$translate(content)",
  "author": "Sarah Johnson",
  "date": "2024-01-15T00:00:00",
  "category_code": "TECH"
}
```

Translations are stored in the `translations` array:

```json
"translations": [
  {"language": "EN", "key": "title", "value": "AI Revolution"},
  {"language": "FR", "key": "title", "value": "Révolution IA"},
  {"language": "ES", "key": "title", "value": "Revolución IA"}
]
```

### 3. Language Switching

When the user selects a language:
1. `changeLanguage(lang)` is called
2. The URL is updated with `?translation=XX`
3. The page reloads
4. All content is fetched with the `translation` parameter
5. The Nodify API resolves `$translate()` directives automatically

### 4. Article Loading

- **Metadata**: First fetch article metadata to get codes
- **Content**: Then fetch translated content for each article using `payloadOnly=true`
- **Display**: Articles are displayed with their translated titles, summaries, and authors

### 5. Comment System

- Comments are stored as `Data` objects with `dataType: 'COMMENT'`
- Each comment is linked to an article via `contentNodeCode`
- Comments are retrieved and displayed when opening an article

## 📖 Usage

### Running the Example

```bash
python news_feed_example.py
```

### Expected Output

```
============================================================
NODIFY NEWS FEED EXAMPLE - Python Client
============================================================

🔐 Authenticating...
✅ Authenticated successfully

📁 Creating main site node...
✅ Site node created: SITE-ABCD1234

📁 Creating categories container node...
✅ Categories container created: CATEGORIES-EFGH5678

📋 Creating categories and articles...
  ✅ Category created: TECH (node: CAT-12345678)
      ✅ Article created: POST-9ABCDEF0
      ✅ Article created: POST-1BCDEF23
  ✅ Category created: SPORTS (node: CAT-456789AB)
      ✅ Article created: POST-2CDEF345
  ✅ Category created: POLITICS (node: CAT-789ABCDE)
      ✅ Article created: POST-3DEF4567

🎨 Creating style node...
✅ Style node created: STYLE-ABCD1234

📜 Creating script node...
✅ Script node created: SCRIPT-EFGH5678

🏠 Creating landing page...
✅ Landing page created: PAGE-12345678

🚀 Publishing all content...
✅ Site node published: SITE-ABCD1234

╔════════════════════════════════════════════════════════════╗
║                    BUILD COMPLETE!                         ║
╚════════════════════════════════════════════════════════════╝

📊 Site Node: SITE-ABCD1234
  ├─ 🎨 Style: STYLE-ABCD1234
  ├─ 📜 Script: SCRIPT-EFGH5678
  ├─ 📄 Landing Page: PAGE-12345678
  └─ 📁 Categories Container: CATEGORIES-EFGH5678
      └─ 📁 Category: TECH (node: CAT-12345678)
          └─ 📝 POST-9ABCDEF0
          └─ 📝 POST-1BCDEF23
      └─ 📁 Category: SPORTS (node: CAT-456789AB)
          └─ 📝 POST-2CDEF345
      └─ 📁 Category: POLITICS (node: CAT-789ABCDE)
          └─ 📝 POST-3DEF4567

🌐 Landing Page URL: http://localhost:8080/v0/content-node/code/PAGE-12345678
💡 Change language via the selector at the top right corner (EN, FR, ES)
```

### Accessing the Landing Page

Open your browser and navigate to the displayed URL:

```
http://localhost:8080/v0/content-node/code/PAGE-XXXXXX
```

To change language, use the language selector in the top-right corner or add `?translation=FR` to the URL.

## 🔌 API Endpoints Used

| Endpoint | Purpose |
|----------|---------|
| `POST /v0/nodes/` | Create nodes (site, categories container, categories) |
| `POST /v0/content-node/` | Create content nodes (style, script, landing page, articles) |
| `GET /v0/nodes/parent/{code}` | Get child nodes of a parent (categories) |
| `GET /contents/node/code/{code}?status=PUBLISHED` | Get article metadata (codes) |
| `GET /contents/code/{code}?payloadOnly=true&status=PUBLISHED&translation={lang}` | Get translated article content |
| `POST /v0/datas/` | Add comments |
| `GET /v0/datas/contentCode/{code}` | Retrieve comments |
| `POST /v0/nodes/code/{code}/publish` | Publish nodes |

## 🎨 Customization

### Adding More Categories

Add new entries to the `categories_data` array in `create_categories_and_articles`:

```python
categories_data = [
    {"code": "TECH", "name": "Technology", "description": "Latest in technology"},
    {"code": "SPORTS", "name": "Sports", "description": "Sports news"},
    {"code": "POLITICS", "name": "Politics", "description": "Political news"},
    {"code": "ENTERTAINMENT", "name": "Entertainment", "description": "Entertainment news"}  # New category
]
```

### Adding More Articles

Add new entries to the `articles_with_translations` dictionary:

```python
articles_with_translations = {
    "TECH": [
        # Existing articles...
        {
            "title_en": "New Tech Article",
            "title_fr": "Nouvel article tech",
            "title_es": "Nuevo artículo tech",
            # ... add all translations
        }
    ],
    # ...
}
```

### Modifying Styles

Edit the `self.css_content` string in the `__init__` method to customize the appearance.

### Changing Colors

Modify the gradient in the CSS:

```python
background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
```

## 🐛 Troubleshooting

### Common Issues

**Issue**: Connection refused or timeout
- **Solution**: Ensure your Nodify instance is running and accessible
- Check URLs: `http://localhost:8080` and `http://localhost:1080`

**Issue**: Login failed
- **Solution**: Verify credentials are `admin` / `Admin13579++`
- Check that the Nodify instance is properly initialized

**Issue**: Articles not displaying
- **Solution**: Check that articles were created correctly
- Verify in Nodify Studio that content nodes exist and are published

**Issue**: Translations not working
- **Solution**: Ensure the `translation` parameter is passed in API calls
- Check that articles have their `translations` array populated

**Issue**: Comments not saving
- **Solution**: Verify that the Data API endpoint is available
- Check that `contentNodeCode` matches the article code

### Debugging

Enable debug logging by adding `print` statements or use browser developer tools to inspect network requests.

## 📄 License

This example is provided under the MIT License. The Nodify CMS and its client libraries have their own licenses.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on [GitHub](https://github.com/AZIRARM/nodify-clients).

## 📚 Related Resources

- [Nodify Python Client](https://github.com/AZIRARM/nodify-clients/tree/main/python)
- [Nodify Node.js Client](https://github.com/AZIRARM/nodify-clients/tree/main/node)
- [Nodify Headless CMS Documentation](https://nodify.azirar.ovh/docs)

---

Built with ❤️ using [Nodify Headless CMS](https://nodify.azirar.ovh)
