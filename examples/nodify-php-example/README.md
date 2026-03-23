```markdown
# Nodify E-Commerce Example - PHP Client

A complete e-commerce shop built with the Nodify PHP client. This example demonstrates how to create a fully functional online store with categories, products, reviews, and a shopping cart using Nodify Headless CMS.

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

This example creates a dynamic e-commerce website with:

- **Categories**: Video Games, Energy Drinks, Consoles, Sales, Promotions
- **Products**: Each product has title, description, price, and multiple images
- **Reviews**: Users can rate products (1-5 stars) and leave comments
- **Shopping Cart**: Persistent cart stored in localStorage
- **Multilingual**: EN, FR, ES translations for all content
- **Dynamic Loading**: Categories and products loaded dynamically via API

## ✨ Features

- **Hierarchical Content Structure**: Site → Categories → Products → Images
- **Multilingual Support**: EN, FR, ES translations using `$translate()` directives
- **Product Reviews**: Users can add ratings and comments (stored as Data objects)
- **Shopping Cart**: Cart stored in browser localStorage, persists across reloads
- **Category Filtering**: Click category buttons to filter products
- **Product Modal**: Click any product to view details, images, and reviews
- **Image Management**: Multiple images per product stored in category-specific folders
- **Responsive Design**: Mobile-friendly interface
- **Dynamic Codes**: All node and content codes generated dynamically

## 📋 Prerequisites

Before running this example, you need:

1. **PHP 7.4 or higher** installed
2. **Composer** (PHP package manager)
3. **Nodify CMS Instance** running:
   - Core URL: `http://localhost:8080` (or your instance)
   - API URL: `http://localhost:1080` (or your instance)
4. **Default credentials**: `admin` / `Admin13579++`

## 🚀 Installation

### 1. Create Project Directory

```bash
mkdir nodify-ecommerce-example
cd nodify-ecommerce-example
```

### 2. Install Nodify PHP Client via Composer

```bash
composer require nodify/php-client
```

If you don't have Composer installed, download it from [https://getcomposer.org/](https://getcomposer.org/).

### 3. Create the Example File

Create a file named `ecommerce_example.php` with the code provided above.

### 4. Run the Example

```bash
php ecommerce_example.php
```

## 📁 Project Structure

```
nodify-ecommerce-example/
├── composer.json              # Composer dependencies
├── ecommerce_example.php      # Main example script
├── README.md                  # This file
└── vendor/                    # Composer dependencies (generated)
    └── nodify/
        └── php-client/         # Nodify PHP client library
```

## 🔧 How It Works

### Content Structure Created

```
📁 Site Node (SITE-XXXXXX)
├── 🎨 Style Node (STYLE-XXXXXX)           # CSS styles
├── 📜 Script Node (SCRIPT-XXXXXX)         # JavaScript code
├── 📄 Landing Page (PAGE-XXXXXX)          # HTML page
└── 📁 Categories Container (CATEGORIES-XXXXXX)
    └── 📋 Categories Metadata (JSON)      # Category list with translations
        ├── 📁 Category: GAMES (CAT-XXXXXX)
        │   ├── 📁 Images (IMAGES-XXXXXX)
        │   │   ├── 🖼️ Product Image 1
        │   │   └── 🖼️ Product Image 2
        │   ├── 📦 Product: Cyberpunk 2077
        │   └── 📦 Product: Elden Ring
        ├── 📁 Category: ENERGY (CAT-XXXXXX)
        │   ├── 📁 Images
        │   ├── 📦 Product: Monster Energy
        │   └── 📦 Product: Red Bull
        ├── 📁 Category: CONSOLES (CAT-XXXXXX)
        │   ├── 📁 Images
        │   ├── 📦 Product: PlayStation 5
        │   └── 📦 Product: Xbox Series X
        ├── 📁 Category: SALES (CAT-XXXXXX)
        │   ├── 📁 Images
        │   └── 📦 Product: Winter Sale Bundle
        └── 📁 Category: PROMO (CAT-XXXXXX)
            ├── 📁 Images
            └── 📦 Product: Limited Edition Controller
```

### Translation Mechanism

Products use the `$translate()` directive:

```json
{
  "title": "$translate(title)",
  "description": "$translate(description)",
  "price": 59.99,
  "category_code": "GAMES",
  "imageCodes": ["IMG-XXXXXX", "IMG-XXXXXX"]
}
```

Translations are stored in the `translations` array:

```json
"translations": [
  {"language": "EN", "key": "title", "value": "Cyberpunk 2077"},
  {"language": "FR", "key": "title", "value": "Cyberpunk 2077"},
  {"language": "ES", "key": "title", "value": "Cyberpunk 2077"}
]
```

### Review System

- Reviews are stored as `Data` objects with `dataType: 'REVIEW'`
- Each review contains: user name, rating (1-5), comment, and date
- Rating is displayed as stars (★) in the UI
- Reviews are retrieved and displayed when opening a product modal

### Shopping Cart

- Cart is stored in browser `localStorage`
- Each cart item has: product code, title, price, quantity
- Cart count updates dynamically
- Cart modal shows items, quantities, and total price

## 📖 Usage

### Running the Example

```bash
php ecommerce_example.php
```

### Expected Output

```
============================================================
NODIFY E-COMMERCE EXAMPLE - PHP Client
============================================================

🔐 Authenticating...
✅ Authenticated successfully

📁 Creating main site node...
✅ Site node created: SITE-ABCD1234

📁 Creating categories container node...
✅ Categories container created: CATEGORIES-EFGH5678

📋 Creating categories and products...
  ✅ Category created: GAMES (node: CAT-12345678)
      ✅ Product created: Cyberpunk 2077 (code: PROD-ABCDEF12)
      ✅ Product created: Elden Ring (code: PROD-34567890)
  ✅ Category created: ENERGY (node: CAT-23456789)
      ✅ Product created: Monster Energy (code: PROD-45678901)
      ✅ Product created: Red Bull (code: PROD-56789012)
  ...

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
      └─ 📁 Category: GAMES (node: CAT-12345678)
          └─ 📦 Product: Cyberpunk 2077 (code: PROD-ABCDEF12)
          └─ 📦 Product: Elden Ring (code: PROD-34567890)
      ...

🌐 Landing Page URL: http://localhost:8080/v0/content-node/code/PAGE-12345678
💡 Change language via the selector at the top right corner (EN, FR, ES)
💡 Click on any product to view details, add reviews, and add to cart
💡 Cart is saved in localStorage - persists across page reloads

✅ Done!
```

### Accessing the E-Commerce Shop

Open your browser and navigate to the displayed URL:

```
http://localhost:8080/v0/content-node/code/PAGE-XXXXXX
```

### Features in Action

| Feature | How to Use |
|---------|------------|
| Change Language | Use the language selector at the top-right corner |
| Filter by Category | Click category buttons to show only products from that category |
| View Product | Click any product card to open the modal with details |
| Add Review | Enter your name, select rating, write a comment, and submit |
| Add to Cart | Click "Add to Cart" button on any product |
| View Cart | Click the cart icon in the navigation bar |
| Update Cart | In cart modal, use +/- buttons to adjust quantities |
| Remove Item | Click the trash icon next to any cart item |
| Checkout | Click "Proceed to Checkout" (demo action) |

## 🔌 API Endpoints Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/authentication/login` | POST | Authenticate and get JWT token |
| `/v0/nodes/` | POST | Create nodes (site, categories, image folders) |
| `/v0/content-node/` | POST | Create content nodes (style, script, HTML, products) |
| `/v0/nodes/code/{code}/publish` | POST | Publish nodes |
| `/v0/datas/` | POST | Add product reviews |
| `/v0/nodes/parent/{code}` | GET | Get child nodes (categories) |
| `/contents/node/code/{code}?status=PUBLISHED` | GET | Get product metadata |
| `/contents/code/{code}?payloadOnly=true&status=PUBLISHED&translation={lang}` | GET | Get translated product content |
| `/contents/code/{code}/file?status=PUBLISHED` | GET | Get product images |
| `/datas/contentCode/{code}?limit=50` | GET | Get product reviews |
| `/datas/content-code/{code}/count` | GET | Get review count |

## 🎨 Customization

### Adding More Categories

Modify the `$categoriesData` array in `createCategoriesAndProducts()`:

```php
$categoriesData = [
    // Existing categories...
    [
        "code" => "NEW_CAT",
        "name_en" => "New Category",
        "name_fr" => "Nouvelle Catégorie",
        "name_es" => "Nueva Categoría",
        "description_en" => "Category description",
        "description_fr" => "Description de la catégorie",
        "description_es" => "Descripción de la categoría",
        "nodeCode" => $this->generateCode('CAT')
    ]
];
```

### Adding More Products

Add new products to the `$productsData` array:

```php
$productsData = [
    "GAMES" => [
        // Existing products...
        [
            "title_en" => "New Game Title",
            "title_fr" => "Nouveau Titre de Jeu",
            "title_es" => "Nuevo Título de Juego",
            "description_en" => "Game description",
            "description_fr" => "Description du jeu",
            "description_es" => "Descripción del juego",
            "price" => 59.99,
            "imageNames" => ["new_game_image_1", "new_game_image_2"]
        ]
    ],
    // Add for other categories...
];
```

### Modifying Styles

Edit the `$cssContent` property in the `EcommerceBuilder` class to customize colors, layout, and animations.

### Changing Colors

Modify the gradient in the CSS:

```css
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
```

## 🐛 Troubleshooting

### Common Issues

**Issue**: Composer not found
- **Solution**: Install Composer from [https://getcomposer.org/](https://getcomposer.org/)
- Alternatively, download the client manually from GitHub

**Issue**: Connection refused or timeout
- **Solution**: Ensure your Nodify instance is running
- Check URLs: `http://localhost:8080` and `http://localhost:1080`

**Issue**: Login failed
- **Solution**: Verify credentials are `admin` / `Admin13579++`
- Check that the Nodify instance is properly initialized

**Issue**: Products not displaying
- **Solution**: Check browser console for errors
- Verify that products were created successfully in the script output
- Ensure all content is published

**Issue**: Images not loading
- **Solution**: Check that image codes are correctly stored in products
- Verify image nodes are published

**Issue**: Reviews not saving
- **Solution**: Check that the Data API endpoint is available
- Verify that `contentNodeCode` matches the product code

**Issue**: Cart not persisting
- **Solution**: Check that localStorage is enabled in your browser
- Verify JavaScript is not blocked

### Debugging

- Open browser developer tools (F12)
- Check the Console tab for JavaScript errors
- Check the Network tab for API request responses
- Verify that all requests return HTTP 200 status

## 📄 License

This example is provided under the MIT License. The Nodify PHP client and Nodify CMS have their own licenses.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues or pull requests on [GitHub](https://github.com/AZIRARM/nodify-clients).

## 📚 Related Resources

- [Nodify PHP Client Documentation](https://github.com/AZIRARM/nodify-clients/tree/main/php)
- [Nodify Python Client](https://github.com/AZIRARM/nodify-clients/tree/main/python)
- [Nodify Node.js Client](https://github.com/AZIRARM/nodify-clients/tree/main/node)
- [Nodify Headless CMS](https://nodify.azirar.ovh)

---

Built with ❤️ using [Nodify Headless CMS](https://nodify.azirar.ovh)
```