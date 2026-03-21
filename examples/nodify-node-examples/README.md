```markdown
# Nodify Landing Page Example

A complete example of a multilingual landing page built with the Nodify Node.js client. This example demonstrates how to create a dynamic landing page with articles, features, and images using Nodify Headless CMS.

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

This example creates a complete landing page with the following structure:

```
📁 Site Node (parent)
├── 🎨 Style Node (CSS)<br/>
├── 📜 Script Node (JavaScript)<br/>
├── 📄 Landing Page HTML<br/>
├── 📁 Articles Node<br/>
│   ├── 📝 Article 1 (JSON)<br/>
│   ├── 📝 Article 2 (JSON)<br/>
│   └── 📝 Article 3 (JSON)<br/>
├── 📁 Features Node<br/>
│   ├── 📋 Feature 1 (JSON)<br/>
│   ├── 📋 Feature 2 (JSON)<br/>
│   └── 📋 Feature 3 (JSON)<br/>
└── 📁 Pictures Node<br/>
├── 🖼️ Hero Image<br/>
├── 🖼️ Feature 1 Image<br/>
├── 🖼️ Feature 2 Image<br/>
└── 🖼️ Feature 3 Image<br/>
```

## ✨ Features

- **Multilingual Support**: Built-in translations for EN, FR, ES using `$translate()` directives
- **Dynamic Content**: Articles and features are fetched dynamically from Nodify
- **Responsive Design**: Mobile-friendly layout with smooth animations
- **Language Switcher**: Change language on the fly with URL parameter `?translation=XX`
- **Modal Popups**: Click on articles to view full content in a modal window
- **Image Management**: All images stored in a dedicated Pictures node
- **Hierarchical Structure**: Clean organization with separate nodes for different content types

## 📋 Prerequisites

Before running this example, you need:

1. **Node.js** (v18 or higher)
2. **Nodify CMS Instance** - You can either:
   - Use the public demo: `http://localhost:8080` (core) and `http://localhost:1080` (api)
   - Run your own instance via Docker (see [Nodify documentation](https://github.com/AZIRARM/nodify-clients))

3. **Nodify Node.js Client** installed:
   ```bash
   npm install nodify-node-client
   ```

## 🚀 Installation

1. **Clone the repository** (or create the files manually):

```bash
git clone https://github.com/AZIRARM/nodify-clients.git
cd examples/nodify-node-examples
```

2. **Install dependencies**:

```bash
npm install
```

3. **Configure the connection** (if needed):

Edit `landing-page-example.ts` to update the Nodify URLs:

```typescript
const builder = new LandingPageBuilder({
  coreUrl: 'http://localhost:8080',  // Nodify Core URL
  apiUrl: 'http://localhost:1080'    // Nodify API URL
});
```

4. **Run the example**:

```bash
npm run landing-page
```

## 📁 Project Structure

```
landing-page/
├── src/
│   └── landing-page-example.ts    # Main example file
├── package.json                   # Dependencies and scripts
├── tsconfig.json                  # TypeScript configuration
└── README.md                      # This file
```

## 🔧 How It Works

### 1. Content Creation

The builder creates the following content in Nodify:

- **Site Node**: Parent container for all content
- **Pictures Node**: Stores all images as `PICTURE` type content nodes
- **Features Node**: Stores feature data as `JSON` type content nodes with translations
- **Articles Node**: Stores article data as `JSON` type content nodes with translations
- **Style Node**: CSS styles as `STYLE` type content node
- **Script Node**: JavaScript code as `SCRIPT` type content node
- **Landing Page**: HTML page as `HTML` type content node

### 2. Translation Mechanism

All translatable content uses the `$translate(KEY)` directive:

```json
{
  "title": "$translate(ARTICLE_1_TITLE)",
  "description": "$translate(ARTICLE_1_DESC)",
  "content": "$translate(ARTICLE_1_CONTENT)"
}
```

Translations are stored in the `translations` array of each content node:

```typescript
translations: [
  { key: 'ARTICLE_1_TITLE', language: 'EN', value: 'Getting Started' },
  { key: 'ARTICLE_1_TITLE', language: 'FR', value: 'Démarrer' },
  { key: 'ARTICLE_1_TITLE', language: 'ES', value: 'Comenzando' }
]
```

### 3. Language Switching

When the user selects a language:

1. `changeLanguage(lang)` is called
2. The URL is updated with `?translation=XX`
3. The page reloads
4. All content is fetched with the `translation` parameter, and `$translate()` directives are resolved by the API

### 4. Content Loading

- **Features**: Fetch metadata first, then content with translations
- **Articles**: Fetch metadata first, then content with translations
- **Images**: Direct URL `/contents/code/{imageCode}/file`

## 📖 Usage

### Running the Example

```bash
# Development mode (with ts-node)
npm run landing-page

# Build and run
npm run build
npm start
```

### Accessing the Landing Page

After successful execution, the console will display URLs:

```
🌐 Landing Page (EN): http://localhost:8080/v0/content-node/code/PAGE-XXXXXX
🌐 Landing Page (FR): http://localhost:8080/v0/content-node/code/PAGE-XXXXXX?translation=FR
🌐 Landing Page (ES): http://localhost:8080/v0/content-node/code/PAGE-XXXXXX?translation=ES
```

### Changing Language

- Use the language selector in the top-right corner
- Or manually add `?translation=FR` to the URL

## 🔌 API Endpoints

The example uses the following Nodify API endpoints:

| Endpoint | Purpose |
|----------|---------|
| `GET /contents/node/code/{nodeCode}?status=PUBLISHED` | Get content metadata (codes) |
| `GET /contents/code/{contentCode}?payloadOnly=true&status=PUBLISHED&translation={lang}` | Get translated content |
| `GET /contents/code/{imageCode}/file?status=PUBLISHED` | Get image file |
| `POST /v0/nodes/` | Create nodes |
| `POST /v0/content-node/` | Create content nodes |
| `POST /v0/nodes/code/{code}/publish` | Publish nodes |

## 🎨 Customization

### Adding More Articles

Modify the `articlesData` array in `createArticles()`:

```typescript
{
  code: this.generateCode('POST'),
  titleKey: 'ARTICLE_4_TITLE',
  descriptionKey: 'ARTICLE_4_DESC',
  contentKey: 'ARTICLE_4_CONTENT',
  author: 'New Author',
  date: new Date().toISOString(),
  imageName: 'Feature 1 Image',
  tags: ['new', 'tag']
}
```

### Adding Translations for New Articles

Add new entries to the `articleTranslations` array:

```typescript
{ key: 'ARTICLE_4_TITLE', language: 'EN', value: 'New Article Title' },
{ key: 'ARTICLE_4_TITLE', language: 'FR', value: 'Nouveau Titre' },
{ key: 'ARTICLE_4_TITLE', language: 'ES', value: 'Nuevo Título' }
```

### Modifying Styles

Edit the CSS in `createStyleNode()` to customize the appearance.

### Adding New Images

Add new entries to the `images` array in `createImages()`:

```typescript
{ name: 'New Image', alt: 'Description', url: 'https://example.com/image.jpg', width: 800, height: 600 }
```

## 🐛 Troubleshooting

### Common Issues

**Issue**: Articles don't load
- **Solution**: Check that the Articles node is published: `await client.publishNode(articlesNode.code!)`

**Issue**: Images not displaying
- **Solution**: Verify that image codes are correctly stored in articles and features

**Issue**: Translations not working
- **Solution**: Ensure the `translation` parameter is passed in all API calls

**Issue**: Modal doesn't open
- **Solution**: Check that `article.code` is correctly passed from the card click handler

### Debugging

Open the browser console to see:
- Fetched articles and features data
- API request URLs
- Any errors during loading

## 📄 License

This example is licensed under the MIT License. The Nodify Node.js client is licensed under CC BY-NC 4.0.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on [GitHub](https://github.com/AZIRARM/nodify-clients).

## 📚 Related Resources

- [Nodify Node.js Client Documentation](https://github.com/AZIRARM/nodify-clients)

---

Built with ❤️ using [Nodify Headless CMS](https://nodify.azirar.ovh)
```