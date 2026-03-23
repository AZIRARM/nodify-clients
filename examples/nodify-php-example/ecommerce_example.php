<?php
/**
 * Nodify E-Commerce Example - PHP Client
 * A complete e-commerce shop with categories, products, cart, and checkout
 */

require_once __DIR__ . '/vendor/autoload.php';

use Nodify\Client;
use Nodify\Node;
use Nodify\ContentNode;
use Nodify\Translation;
use Nodify\Value;
use Nodify\ContentTypeEnum;
use Nodify\StatusEnum;
use Nodify\Exception\NodifyClientException;

class EcommerceBuilder
{
    private Client $client;
    private string $coreUrl;
    private string $apiUrl;

    private ?Node $siteNode = null;
    private ?Node $categoriesContainer = null;
    private ?ContentNode $styleNode = null;
    private ?ContentNode $scriptNode = null;
    private ?ContentNode $landingPage = null;
    private array $categories = [];
    private array $products = [];

    private string $cssContent = <<<'CSS'
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: #333;
        }

        .language-selector {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 1001;
            background: white;
            padding: 8px 15px;
            border-radius: 25px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .language-selector select {
            padding: 5px 10px;
            border-radius: 15px;
            border: 1px solid #667eea;
            cursor: pointer;
            font-size: 14px;
            background: white;
        }

        .navbar {
            background: rgba(255,255,255,0.95);
            backdrop-filter: blur(10px);
            box-shadow: 0 2px 20px rgba(0,0,0,0.1);
            position: sticky;
            top: 0;
            z-index: 1000;
        }

        .nav-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 1rem 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 1rem;
        }

        .logo {
            font-size: 1.5rem;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .nav-links {
            display: flex;
            gap: 2rem;
            list-style: none;
            align-items: center;
        }

        .nav-links a {
            text-decoration: none;
            color: #666;
            transition: color 0.3s;
            font-weight: 500;
            cursor: pointer;
            padding: 0.5rem 1rem;
            border-radius: 25px;
        }

        .nav-links a:hover {
            color: #667eea;
            background: rgba(102, 126, 234, 0.1);
        }

        .cart-link {
            position: relative;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }

        .cart-count {
            background: #667eea;
            color: white;
            border-radius: 20px;
            padding: 2px 8px;
            font-size: 0.75rem;
            font-weight: bold;
            margin-left: 5px;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 2rem;
        }

        .hero {
            text-align: center;
            padding: 4rem 2rem;
            background: rgba(255,255,255,0.95);
            border-radius: 20px;
            margin-bottom: 3rem;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
        }

        .hero h1 {
            font-size: 3rem;
            margin-bottom: 1rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .hero p {
            font-size: 1.2rem;
            color: #666;
            margin-bottom: 2rem;
        }

        /* Categories filters */
        .category-filters {
            display: flex;
            gap: 1rem;
            margin-bottom: 2rem;
            flex-wrap: wrap;
            justify-content: center;
            padding: 0.5rem;
        }

        .category-btn {
            padding: 0.75rem 1.75rem;
            border: none;
            border-radius: 40px;
            cursor: pointer;
            font-size: 0.95rem;
            font-weight: 600;
            transition: all 0.3s ease;
            background: white;
            color: #4a5568;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            letter-spacing: 0.3px;
        }

        .category-btn:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(0,0,0,0.15);
            background: #f8f9fa;
            color: #667eea;
        }

        .category-btn.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }

        /* Products grid */
        .products-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 2rem;
            margin-bottom: 2rem;
        }

        .product-card {
            background: white;
            border-radius: 16px;
            overflow: hidden;
            transition: transform 0.3s, box-shadow 0.3s;
            cursor: pointer;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }

        .product-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
        }

        .product-card img {
            width: 100%;
            height: 220px;
            object-fit: cover;
            transition: transform 0.5s;
        }

        .product-card:hover img {
            transform: scale(1.05);
        }

        .product-content {
            padding: 1.5rem;
        }

        .product-category-badge {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 0.25rem 1rem;
            border-radius: 20px;
            font-size: 0.7rem;
            font-weight: 500;
            margin-bottom: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .product-card h3 {
            margin-bottom: 0.5rem;
            color: #2d3748;
            font-size: 1.25rem;
            font-weight: 600;
            line-height: 1.3;
        }

        .product-description {
            color: #718096;
            font-size: 0.9rem;
            line-height: 1.5;
            margin-bottom: 1rem;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .product-price {
            font-size: 1.5rem;
            font-weight: bold;
            color: #667eea;
            margin: 0.75rem 0;
        }

        .add-to-cart {
            display: inline-block;
            width: 100%;
            margin-top: 0.5rem;
            padding: 0.75rem 1rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.3s;
            text-align: center;
        }

        .add-to-cart:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        /* Modal */
        .modal {
            display: none;
            position: fixed;
            z-index: 2000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.85);
            overflow-y: auto;
        }

        .modal-content {
            background: white;
            margin: 40px auto;
            padding: 2rem;
            border-radius: 24px;
            max-width: 700px;
            position: relative;
            animation: slideDown 0.3s ease;
        }

        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .close {
            position: absolute;
            right: 20px;
            top: 20px;
            font-size: 28px;
            cursor: pointer;
            color: #999;
            transition: color 0.3s;
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }

        .close:hover {
            color: #333;
            background: #f0f0f0;
        }

        .modal-product-image {
            width: 100%;
            max-height: 300px;
            object-fit: cover;
            border-radius: 16px;
            margin-bottom: 1rem;
        }

        .modal-product-price {
            font-size: 2rem;
            font-weight: bold;
            color: #667eea;
            margin: 1rem 0;
        }

        /* Cart modal */
        .cart-modal {
            max-width: 600px;
        }

        .cart-items {
            max-height: 400px;
            overflow-y: auto;
            margin: 1rem 0;
        }

        .cart-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem;
            border-bottom: 1px solid #eee;
            gap: 1rem;
            flex-wrap: wrap;
        }

        .cart-item-info {
            flex: 2;
        }

        .cart-item-title {
            font-weight: 600;
            margin-bottom: 0.25rem;
        }

        .cart-item-price {
            color: #667eea;
            font-size: 0.9rem;
        }

        .cart-item-actions {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .cart-item-actions button {
            width: 32px;
            height: 32px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: bold;
            transition: background 0.2s;
        }

        .cart-item-actions button:hover {
            background: #5a67d8;
        }

        .cart-item-quantity {
            min-width: 40px;
            text-align: center;
            font-weight: 600;
        }

        .cart-item-total {
            font-weight: bold;
            min-width: 80px;
            text-align: right;
            color: #2d3748;
        }

        .cart-total {
            text-align: right;
            padding: 1rem;
            font-size: 1.3rem;
            font-weight: bold;
            border-top: 2px solid #eee;
            margin-top: 1rem;
        }

        .checkout-btn {
            width: 100%;
            padding: 1rem;
            background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
            color: white;
            border: none;
            border-radius: 12px;
            cursor: pointer;
            font-size: 1.1rem;
            font-weight: bold;
            transition: all 0.3s;
            margin-top: 1rem;
        }

        .checkout-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(72, 187, 120, 0.4);
        }

        /* Footer */
        .footer {
            background: rgba(255,255,255,0.95);
            text-align: center;
            padding: 2rem;
            margin-top: 4rem;
            border-radius: 20px;
            color: #4a5568;
        }

        /* Reviews section */
        .review-section {
            margin-top: 2rem;
            padding-top: 1rem;
            border-top: 1px solid #eee;
        }

        .review-section h4 {
            font-size: 1.2rem;
            margin-bottom: 1rem;
            color: #2d3748;
        }

        .review-input {
            display: flex;
            flex-direction: column;
            gap: 0.75rem;
            margin-top: 1rem;
            background: #f7fafc;
            padding: 1rem;
            border-radius: 12px;
        }

        .review-input input,
        .review-input select,
        .review-input textarea {
            padding: 0.75rem;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            font-size: 0.95rem;
            transition: border-color 0.2s;
        }

        .review-input input:focus,
        .review-input select:focus,
        .review-input textarea:focus {
            outline: none;
            border-color: #667eea;
        }

        .review-input button {
            padding: 0.75rem 1.5rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.3s;
        }

        .review-input button:hover {
            transform: translateY(-2px);
        }

        .review {
            background: #f7fafc;
            padding: 1rem;
            margin: 0.75rem 0;
            border-radius: 12px;
            border-left: 3px solid #667eea;
        }

        .review strong {
            color: #2d3748;
        }

        .review-rating {
            color: #fbbf24;
            font-size: 0.9rem;
            margin-left: 0.5rem;
        }

        .stars {
            color: #fbbf24;
            font-size: 1rem;
            letter-spacing: 2px;
        }

        .rating {
            margin: 1rem 0;
            padding: 0.75rem;
            background: #fef3c7;
            border-radius: 12px;
            text-align: center;
        }

        /* Responsive */
        @media (max-width: 768px) {
            .nav-container {
                flex-direction: column;
                text-align: center;
            }

            .nav-links {
                justify-content: center;
                gap: 1rem;
                flex-wrap: wrap;
            }

            .language-selector {
                position: relative;
                top: 0;
                right: 0;
                display: inline-block;
                margin-bottom: 1rem;
            }

            .hero {
                padding: 2rem 1.5rem;
            }

            .hero h1 {
                font-size: 2rem;
            }

            .hero p {
                font-size: 1rem;
            }

            .category-filters {
                gap: 0.75rem;
            }

            .category-btn {
                padding: 0.6rem 1.25rem;
                font-size: 0.85rem;
            }

            .products-grid {
                grid-template-columns: 1fr;
                gap: 1.5rem;
            }

            .modal-content {
                margin: 20px;
                padding: 1.5rem;
            }

            .cart-item {
                flex-direction: column;
                text-align: center;
            }

            .cart-item-total {
                text-align: center;
            }

            .cart-item-actions {
                justify-content: center;
            }

            .product-description {
                -webkit-line-clamp: 3;
            }
        }

        @media (max-width: 480px) {
            .category-filters {
                gap: 0.5rem;
            }

            .category-btn {
                padding: 0.5rem 1rem;
                font-size: 0.75rem;
            }

            .product-card h3 {
                font-size: 1.1rem;
            }

            .product-price {
                font-size: 1.2rem;
            }

            .product-description {
                font-size: 0.85rem;
            }

            .modal-product-price {
                font-size: 1.5rem;
            }
        }

        @keyframes fadeInOut {
            0% { opacity: 0; transform: translateY(20px); }
            15% { opacity: 1; transform: translateY(0); }
            85% { opacity: 1; transform: translateY(0); }
            100% { opacity: 0; transform: translateY(-20px); }
        }
    CSS;

    private string $jsContentTemplate = <<<'JS'
        const API_BASE_URL = '{api_url}';
        const CORE_BASE_URL = '{core_url}';
        const CATEGORIES_CONTAINER_CODE = '{categories_container_code}';

        let currentModal = null;
        let currentLang = 'EN';
        let currentCategory = null;
        let cart = JSON.parse(localStorage.getItem('cart') || '[]');
        let productsMap = new Map();

        const urlParams = new URLSearchParams(window.location.search);
        const urlLang = urlParams.get('translation');
        if (urlLang && ['EN', 'FR', 'ES'].includes(urlLang)) {
            currentLang = urlLang;
        } else if (localStorage.getItem('language')) {
            currentLang = localStorage.getItem('language');
        }

        function changeLanguage(lang) {
            if (!lang || lang === currentLang) return;
            currentLang = lang;
            localStorage.setItem('language', lang);
            const url = new URL(window.location.href);
            url.searchParams.set('translation', lang);
            window.location.href = url.toString();
        }

        async function fetchCategories() {
            const url = API_BASE_URL + '/nodes/parent/' + CATEGORIES_CONTAINER_CODE;
            const response = await fetch(url);
            return response.json();
        }

        async function fetchProductsFromCategory(categoryCode) {
            const url = API_BASE_URL + '/contents/node/code/' + categoryCode + '?status=PUBLISHED&translation=' + currentLang;
            const response = await fetch(url);
            const items = await response.json();

            return items.map(item => {
                let content = {};
                try {
                    content = item.payload;
                } catch(e) {
                    console.error('Error parsing content', e);
                }

                return {
                    code: item.code,
                    title: content.title,
                    description: content.description,
                    price: content.price || 0,
                    imageCodes: content.imageCodes || [],
                    category_code: content.category_code
                };
            });
        }

        async function fetchImages(imageCodes) {
            const images = [];
            for (const code of imageCodes) {
                const url = API_BASE_URL + '/contents/code/' + code + '/file?status=PUBLISHED';
                images.push(url);
            }
            return images;
        }

        function addToCart(product) {
            const existing = cart.find(item => item.code === product.code);
            if (existing) {
                existing.quantity++;
            } else {
                cart.push({
                    code: product.code,
                    title: product.title,
                    price: product.price,
                    quantity: 1
                });
            }
            localStorage.setItem('cart', JSON.stringify(cart));
            updateCartCount();
            showNotification(product.title + ' added to cart!');
        }

        function showNotification(message) {
            const notification = document.createElement('div');
            notification.textContent = message;
            notification.style.position = 'fixed';
            notification.style.bottom = '20px';
            notification.style.right = '20px';
            notification.style.backgroundColor = '#48bb78';
            notification.style.color = 'white';
            notification.style.padding = '12px 24px';
            notification.style.borderRadius = '8px';
            notification.style.zIndex = '10000';
            notification.style.animation = 'fadeInOut 2s ease';
            document.body.appendChild(notification);

            setTimeout(() => {
                notification.remove();
            }, 2000);
        }

        function updateCartCount() {
            const count = cart.reduce((sum, item) => sum + item.quantity, 0);
            const cartCount = document.getElementById('cartCount');
            if (cartCount) cartCount.textContent = count;
        }

        function openCart() {
            const modal = document.getElementById('cartModal');
            const modalBody = document.getElementById('cartModalBody');

            if (cart.length === 0) {
                modalBody.innerHTML = '<p>Your cart is empty.</p>';
            } else {
                modalBody.innerHTML = `
                    <div class="cart-items">
                        ${cart.map(item => `
                            <div class="cart-item">
                                <div class="cart-item-info">
                                    <div class="cart-item-title">${escapeHtml(item.title)}</div>
                                    <div class="cart-item-price">€${item.price}</div>
                                </div>
                                <div class="cart-item-actions">
                                    <button onclick="updateQuantity('${item.code}', -1)">-</button>
                                    <span class="cart-item-quantity">${item.quantity}</span>
                                    <button onclick="updateQuantity('${item.code}', 1)">+</button>
                                    <button onclick="removeFromCart('${item.code}')">🗑️</button>
                                </div>
                                <div class="cart-item-total">€${(item.price * item.quantity).toFixed(2)}</div>
                            </div>
                        `).join('')}
                    </div>
                    <div class="cart-total">
                        Total: €${cart.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2)}
                    </div>
                    <button class="checkout-btn" onclick="goToCheckout()">Proceed to Checkout</button>
                `;
            }
            modal.style.display = 'block';
            currentModal = modal;
            document.body.style.overflow = 'hidden';
        }

        function updateQuantity(productCode, delta) {
            const item = cart.find(i => i.code === productCode);
            if (item) {
                item.quantity += delta;
                if (item.quantity <= 0) {
                    cart = cart.filter(i => i.code !== productCode);
                }
                localStorage.setItem('cart', JSON.stringify(cart));
                openCart();
                updateCartCount();
            }
        }

        function removeFromCart(productCode) {
            cart = cart.filter(i => i.code !== productCode);
            localStorage.setItem('cart', JSON.stringify(cart));
            openCart();
            updateCartCount();
        }

        function goToCheckout() {
            alert('Checkout page would redirect here. Implement your payment flow.');
        }

        async function openProduct(productCode) {
            const modal = document.getElementById('productModal');
            const modalBody = document.getElementById('productModalBody');

            const product = productsMap.get(productCode);
            if (!product) return;

            const images = await fetchImages(product.imageCodes);
            const mainImage = images[0] || 'https://via.placeholder.com/400x300';

            modalBody.innerHTML = `
                <img src="${mainImage}" alt="${escapeHtml(product.title)}" style="width: 100%; max-height: 300px; object-fit: cover; border-radius: 10px;">
                <h2>${escapeHtml(product.title)}</h2>
                <div class="product-price">€${product.price}</div>
                <p>${escapeHtml(product.description)}</p>
                <button class="add-to-cart" onclick="addToCart({code:'${product.code}',title:'${escapeHtml(product.title)}',price:${product.price}})">Add to Cart</button>
                <div class="review-section">
                    <h4>Reviews</h4>
                    <div id="reviews-${productCode}"></div>
                    <div class="review-input">
                        <input type="text" id="review-name-${productCode}" placeholder="Your name">
                        <select id="review-rating-${productCode}">
                            <option value="5">★★★★★ (5)</option>
                            <option value="4">★★★★☆ (4)</option>
                            <option value="3">★★★☆☆ (3)</option>
                            <option value="2">★★☆☆☆ (2)</option>
                            <option value="1">★☆☆☆☆ (1)</option>
                        </select>
                        <textarea id="review-text-${productCode}" rows="3" placeholder="Your review"></textarea>
                        <button onclick="addReview('${productCode}')">Submit Review</button>
                    </div>
                </div>
            `;

            modal.style.display = 'block';
            currentModal = modal;
            document.body.style.overflow = 'hidden';

            await loadReviews(productCode);
        }

        async function addReview(productCode) {
            const nameInput = document.getElementById('review-name-' + productCode);
            const ratingSelect = document.getElementById('review-rating-' + productCode);
            const textInput = document.getElementById('review-text-' + productCode);

            const reviewData = {
                user: nameInput.value.trim() || 'Anonymous',
                rating: parseInt(ratingSelect.value),
                comment: textInput.value.trim(),
                date: new Date().toISOString()
            };

            if (!reviewData.comment) return;

            const url = API_BASE_URL + '/datas/';
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    contentNodeCode: productCode,
                    dataType: 'REVIEW',
                    name: 'Review',
                    user: reviewData.user,
                    key: 'review_' + Date.now(),
                    value: JSON.stringify(reviewData)
                })
            });

            if (response.ok) {
                nameInput.value = '';
                ratingSelect.value = '5';
                textInput.value = '';
                await loadReviews(productCode);
            }
        }

        async function loadReviews(productCode) {
            const url = API_BASE_URL + '/datas/contentCode/' + productCode + '?limit=50';
            const response = await fetch(url);
            const reviews = await response.json();

            const reviewsDiv = document.getElementById('reviews-' + productCode);
            if (reviewsDiv) {
                if (reviews.length === 0) {
                    reviewsDiv.innerHTML = '<p>No reviews yet. Be the first to review!</p>';
                } else {
                    let totalRating = 0;
                    for (const r of reviews) {
                        let data;
                        try { data = JSON.parse(r.value); } catch(e) { data = { rating: 0 }; }
                        totalRating += (data.rating || 0);
                    }
                    const avgRating = totalRating / reviews.length;
                    let starsHtml = '';
                    for (let i = 0; i < Math.round(avgRating); i++) starsHtml += '★';
                    for (let i = Math.round(avgRating); i < 5; i++) starsHtml += '☆';

                    let reviewsHtml = '';
                    for (const r of reviews) {
                        let data;
                        try { data = JSON.parse(r.value); } catch(e) { data = { user: r.user, rating: 0, comment: r.value, date: '' }; }
                        let reviewStars = '';
                        for (let i = 0; i < data.rating; i++) reviewStars += '★';
                        for (let i = data.rating; i < 5; i++) reviewStars += '☆';
                        reviewsHtml += `
                            <div class="review">
                                <strong>${escapeHtml(data.user)}</strong>
                                <span class="review-rating">${reviewStars}</span>
                                <span style="color:#999; font-size:0.8rem;">${data.date ? new Date(data.date).toLocaleString() : ''}</span>
                                <p>${escapeHtml(data.comment)}</p>
                            </div>
                        `;
                    }

                    reviewsDiv.innerHTML = `
                        <div class="rating">
                            Average Rating: <span class="stars">${starsHtml}</span> (${avgRating.toFixed(1)}/5)
                        </div>
                        ${reviewsHtml}
                    `;
                }
            }
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        function closeModal() {
            if (currentModal) {
                currentModal.style.display = 'none';
                currentModal = null;
                document.body.style.overflow = 'auto';
            }
        }

        function updateStaticTranslations() {
            const elements = document.querySelectorAll('[data-translate]');
            const translations = {
                EN: {
                    MAIN_TITLE: 'Nodify E-Shop',
                    MAIN_SUBTITLE: 'Your one-stop shop for everything',
                    CART: 'Cart',
                    SHOP: 'Shop',
                    FOOTER_TEXT: 'Built with ❤️ using Nodify Headless CMS',
                    COPYRIGHT: '© 2026 Nodify E-Shop. All rights reserved.'
                },
                FR: {
                    MAIN_TITLE: 'Nodify E-Shop',
                    MAIN_SUBTITLE: 'Votre boutique en ligne pour tout',
                    CART: 'Panier',
                    SHOP: 'Boutique',
                    FOOTER_TEXT: 'Construit avec ❤️ en utilisant Nodify Headless CMS',
                    COPYRIGHT: '© 2026 Nodify E-Shop. Tous droits réservés.'
                },
                ES: {
                    MAIN_TITLE: 'Nodify E-Shop',
                    MAIN_SUBTITLE: 'Tu tienda en línea para todo',
                    CART: 'Carrito',
                    SHOP: 'Tienda',
                    FOOTER_TEXT: 'Construido con ❤️ usando Nodify Headless CMS',
                    COPYRIGHT: '© 2026 Nodify E-Shop. Todos los derechos reservados.'
                }
            };
            elements.forEach(el => {
                const key = el.getAttribute('data-translate');
                if (translations[currentLang] && translations[currentLang][key]) {
                    el.textContent = translations[currentLang][key];
                }
            });
        }

        async function loadProducts() {
            const grid = document.getElementById('productsGrid');
            if (!grid) return;

            try {
                const categories = await fetchCategories();

                const filterContainer = document.getElementById('categoryFilters');
                let filterHtml = `<button class="category-btn${!currentCategory ? ' active' : ''}" onclick="filterByCategory(null)">All</button>`;
                for (const cat of categories) {
                    const isActive = currentCategory === cat.code ? ' active' : '';
                    filterHtml += `
                        <button class="category-btn${isActive}" onclick="filterByCategory('${cat.code}')">
                            ${escapeHtml(cat.name)}
                        </button>
                    `;
                }
                filterContainer.innerHTML = filterHtml;

                let allProducts = [];
                productsMap.clear();

                for (const cat of categories) {
                    const products = await fetchProductsFromCategory(cat.code);
                    for (const p of products) {
                        const productData = {
                            code: p.code,
                            title: p.title,
                            description: p.description,
                            price: p.price,
                            imageCodes: p.imageCodes || [],
                            categoryName: cat.name,
                            categoryCode: cat.code
                        };
                        allProducts.push(productData);
                        productsMap.set(p.code, productData);
                    }
                }

                if (currentCategory) {
                    allProducts = allProducts.filter(p => p.categoryCode === currentCategory);
                }

                if (allProducts.length === 0) {
                    grid.innerHTML = '<p style="color: white; text-align: center;">No products found.</p>';
                    return;
                }

                let productHtmls = [];
                for (const product of allProducts) {
                    const images = await fetchImages(product.imageCodes);
                    const mainImage = images[0] || 'https://via.placeholder.com/280x200';
                    productHtmls.push(`
                        <div class="product-card" onclick="openProduct('${product.code}')">
                            <img src="${mainImage}" alt="${escapeHtml(product.title)}" loading="lazy">
                            <div class="product-content">
                                <h3>${escapeHtml(product.title)}</h3>
                                <div class="product-price">€${product.price}</div>
                                <button class="add-to-cart" onclick="event.stopPropagation(); addToCart({code:'${product.code}',title:'${escapeHtml(product.title)}',price:${product.price}})">Add to Cart</button>
                            </div>
                        </div>
                    `);
                }
                grid.innerHTML = productHtmls.join('');
            } catch (error) {
                console.error('Error loading products:', error);
                grid.innerHTML = '<p style="color: white; text-align: center;">Error loading products.</p>';
            }
        }

        function filterByCategory(categoryCode) {
            currentCategory = categoryCode;
            loadProducts();
        }

        document.addEventListener('DOMContentLoaded', async function() {
            updateStaticTranslations();
            updateCartCount();
            await loadProducts();

            const select = document.getElementById('langSelect');
            if (select) {
                select.value = currentLang;
                select.onchange = function(e) { changeLanguage(e.target.value); };
            }
        });

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') closeModal();
        });

        window.onclick = function(e) {
            if (currentModal && e.target === currentModal) closeModal();
        };
    JS;

    public function __construct(string $coreUrl = "https://nodify-core.azirar.ovh", string $apiUrl = "https://nodify-api.azirar.ovh")
    {
        $this->coreUrl = $coreUrl;
        $this->apiUrl = $apiUrl;

        $config = Client::builder()
            ->withBaseUrl($coreUrl)
            ->withTimeout(30000)
            ->build();

        $this->client = Client::create($config);
    }

    private function generateCode(string $prefix): string
    {
        return $prefix . "-" . strtoupper(substr(uniqid(), -8));
    }

    public function login(): void
    {
        echo "🔐 Authenticating...\n";
        $this->client->login('admin', 'Admin13579++');
        echo "✅ Authenticated successfully\n\n";
    }

    public function createSiteNode(): void
    {
        echo "📁 Creating main site node...\n";

        $node = new Node();
        $node->name = "Nodify E-Shop";
        $node->code = $this->generateCode('SITE');
        $node->type = "SITE";
        $node->defaultLanguage = "EN";
        $node->description = "A complete e-commerce shop built with Nodify PHP client";
        $node->status = StatusEnum::SNAPSHOT;
        $node->languages = ["EN", "FR", "ES"];

        $this->siteNode = $this->client->saveNode($node);
        echo "✅ Site node created: {$this->siteNode->code}\n\n";
    }

    public function createCategoriesContainer(): void
    {
        echo "📁 Creating categories container node...\n";

        $node = new Node();
        $node->parentCode = $this->siteNode->code;
        $node->name = "Categories";
        $node->code = $this->generateCode('CATEGORIES');
        $node->type = "FOLDER";
        $node->status = StatusEnum::SNAPSHOT;
        $node->description = "Container for all product categories";

        $this->categoriesContainer = $this->client->saveNode($node);
        echo "✅ Categories container created: {$this->categoriesContainer->code}\n\n";
    }

    public function createCategoriesAndProducts(): void
    {
        echo "📋 Creating categories and products...\n";

        $categoriesData = [
            [
                "code" => "GAMES",
                "name_en" => "Video Games",
                "name_fr" => "Jeux Vidéo",
                "name_es" => "Videojuegos",
                "description_en" => "Latest video games for all platforms",
                "description_fr" => "Derniers jeux vidéo pour toutes les plateformes",
                "description_es" => "Últimos videojuegos para todas las plataformas"
            ],
            [
                "code" => "ENERGY",
                "name_en" => "Energy Drinks",
                "name_fr" => "Boissons Énergétiques",
                "name_es" => "Bebidas Energéticas",
                "description_en" => "Boost your energy with our selection",
                "description_fr" => "Boostez votre énergie avec notre sélection",
                "description_es" => "Aumenta tu energía con nuestra selección"
            ],
            [
                "code" => "CONSOLES",
                "name_en" => "Consoles",
                "name_fr" => "Consoles",
                "name_es" => "Consolas",
                "description_en" => "Latest gaming consoles",
                "description_fr" => "Dernières consoles de jeux",
                "description_es" => "Últimas consolas de juegos"
            ],
            [
                "code" => "SALES",
                "name_en" => "Sales",
                "name_fr" => "Soldes",
                "name_es" => "Ofertas",
                "description_en" => "Best deals and discounts",
                "description_fr" => "Meilleures offres et réductions",
                "description_es" => "Mejores ofertas y descuentos"
            ],
            [
                "code" => "PROMO",
                "name_en" => "Promotions",
                "name_fr" => "Promotions",
                "name_es" => "Promociones",
                "description_en" => "Special limited-time offers",
                "description_fr" => "Offres spéciales à durée limitée",
                "description_es" => "Ofertas especiales por tiempo limitado"
            ]
        ];

        $productsData = [
            "GAMES" => [
                [
                    "title_en" => "Cyberpunk 2077",
                    "title_fr" => "Cyberpunk 2077",
                    "title_es" => "Cyberpunk 2077",
                    "description_en" => "Open-world action RPG set in a futuristic metropolis",
                    "description_fr" => "Jeu de rôle d'action en monde ouvert dans une métropole futuriste",
                    "description_es" => "RPG de acción de mundo abierto en una metrópolis futurista",
                    "price" => 59.99,
                    "imageNames" => ["cyberpunk_1", "cyberpunk_2"]
                ],
                [
                    "title_en" => "Elden Ring",
                    "title_fr" => "Elden Ring",
                    "title_es" => "Elden Ring",
                    "description_en" => "A challenging action RPG from the creators of Dark Souls",
                    "description_fr" => "Un RPG d'action difficile des créateurs de Dark Souls",
                    "description_es" => "Un RPG de acción desafiante de los creadores de Dark Souls",
                    "price" => 69.99,
                    "imageNames" => ["elden_ring_1", "elden_ring_2"]
                ]
            ],
            "ENERGY" => [
                [
                    "title_en" => "Monster Energy",
                    "title_fr" => "Monster Energy",
                    "title_es" => "Monster Energy",
                    "description_en" => "Classic energy drink for maximum boost",
                    "description_fr" => "Boisson énergétique classique pour un boost maximum",
                    "description_es" => "Bebida energética clásica para un impulso máximo",
                    "price" => 2.99,
                    "imageNames" => ["monster_1"]
                ],
                [
                    "title_en" => "Red Bull",
                    "title_fr" => "Red Bull",
                    "title_es" => "Red Bull",
                    "description_en" => "Gives you wings",
                    "description_fr" => "Vous donne des ailes",
                    "description_es" => "Te da alas",
                    "price" => 2.99,
                    "imageNames" => ["redbull_1"]
                ]
            ],
            "CONSOLES" => [
                [
                    "title_en" => "PlayStation 5",
                    "title_fr" => "PlayStation 5",
                    "title_es" => "PlayStation 5",
                    "description_en" => "Next-gen gaming console",
                    "description_fr" => "Console de jeux nouvelle génération",
                    "description_es" => "Consola de juegos de nueva generación",
                    "price" => 499.99,
                    "imageNames" => ["ps5_1", "ps5_2"]
                ],
                [
                    "title_en" => "Xbox Series X",
                    "title_fr" => "Xbox Series X",
                    "title_es" => "Xbox Series X",
                    "description_en" => "The most powerful Xbox ever",
                    "description_fr" => "La Xbox la plus puissante jamais créée",
                    "description_es" => "La Xbox más poderosa jamás creada",
                    "price" => 499.99,
                    "imageNames" => ["xbox_1", "xbox_2"]
                ]
            ],
            "SALES" => [
                [
                    "title_en" => "Winter Sale Bundle",
                    "title_fr" => "Pack Soldes d'Hiver",
                    "title_es" => "Paquete de Ofertas de Invierno",
                    "description_en" => "Get 3 games for the price of 2",
                    "description_fr" => "3 jeux pour le prix de 2",
                    "description_es" => "3 juegos por el precio de 2",
                    "price" => 99.99,
                    "imageNames" => ["sale_1"]
                ]
            ],
            "PROMO" => [
                [
                    "title_en" => "Limited Edition Controller",
                    "title_fr" => "Manette Édition Limitée",
                    "title_es" => "Control Edición Limitada",
                    "description_en" => "Special edition controller with unique design",
                    "description_fr" => "Manette édition limitée au design unique",
                    "description_es" => "Control edición limitada con diseño único",
                    "price" => 79.99,
                    "imageNames" => ["controller_1"]
                ]
            ]
        ];

        $imageCodes = [];

        foreach ($categoriesData as $catData) {
            $categoryNode = new Node();
            $categoryNode->parentCode = $this->categoriesContainer->code;
            $categoryNode->name = $catData['name_en'];
            $categoryNode->code = $this->generateCode('CAT');
            $categoryNode->type = "FOLDER";
            $categoryNode->status = StatusEnum::SNAPSHOT;
            $categoryNode->description = $catData['description_en'];

            $savedCategory = $this->client->saveNode($categoryNode);

            $imagesSubNode = new Node();
            $imagesSubNode->parentCode = $savedCategory->code;
            $imagesSubNode->name = "Images";
            $imagesSubNode->code = $this->generateCode('IMAGES');
            $imagesSubNode->type = "FOLDER";
            $imagesSubNode->status = StatusEnum::SNAPSHOT;
            $savedImagesNode = $this->client->saveNode($imagesSubNode);

            $imageCodes[$catData['code']] = [];

            $catProducts = $productsData[$catData['code']] ?? [];
            foreach ($catProducts as $product) {
                foreach ($product['imageNames'] as $imgName) {
                    if (!isset($imageCodes[$catData['code']][$imgName])) {
                        $imgNode = new ContentNode();
                        $imgNode->parentCode = $savedImagesNode->code;
                        $imgNode->type = ContentTypeEnum::PICTURE;
                        $imgNode->title = $imgName;
                        $imgNode->code = $this->generateCode('IMG');
                        $imgNode->language = "EN";
                        $imgNode->status = StatusEnum::SNAPSHOT;
                        $imgNode->description = "Image for product";
                        $imgNode->content = "https://picsum.photos/id/" . rand(1, 100) . "/400/300";

                        $savedImg = $this->client->saveContentNode($imgNode);
                        $imageCodes[$catData['code']][$imgName] = $savedImg->code;
                    }
                }
            }

            $this->categories[] = [
                'code' => $catData['code'],
                'name_en' => $catData['name_en'],
                'name_fr' => $catData['name_fr'],
                'name_es' => $catData['name_es'],
                'description_en' => $catData['description_en'],
                'description_fr' => $catData['description_fr'],
                'description_es' => $catData['description_es'],
                'nodeCode' => $savedCategory->code
            ];
            echo "  ✅ Category created: {$catData['code']} (node: {$savedCategory->code})\n";

            $catProducts = $productsData[$catData['code']] ?? [];
            foreach ($catProducts as $product) {
                $productCode = $this->generateCode('PROD');

                $imageCodesList = [];
                foreach ($product['imageNames'] as $imgName) {
                    $imageCodesList[] = $imageCodes[$catData['code']][$imgName];
                }

                $productPayload = [
                    "title" => '$translate(title)',
                    "description" => '$translate(description)',
                    "price" => $product['price'],
                    "category_code" => $catData['code'],
                    "imageCodes" => $imageCodesList
                ];

                $translationsList = [
                    new Translation("title", "EN", $product['title_en']),
                    new Translation("title", "FR", $product['title_fr']),
                    new Translation("title", "ES", $product['title_es']),
                    new Translation("description", "EN", $product['description_en']),
                    new Translation("description", "FR", $product['description_fr']),
                    new Translation("description", "ES", $product['description_es'])
                ];

                $contentNode = new ContentNode();
                $contentNode->parentCode = $savedCategory->code;
                $contentNode->type = ContentTypeEnum::JSON;
                $contentNode->title = $product['title_en'];
                $contentNode->code = $productCode;
                $contentNode->language = "EN";
                $contentNode->status = StatusEnum::SNAPSHOT;
                $contentNode->description = $product['description_en'];
                $contentNode->content = json_encode($productPayload);
                $contentNode->translations = $translationsList;

                $savedProduct = $this->client->saveContentNode($contentNode);
                $this->products[] = [
                    'code' => $savedProduct->code,
                    'title' => $product['title_en'],
                    'category_code' => $catData['code']
                ];
                echo "      ✅ Product created: {$product['title_en']} (code: {$savedProduct->code})\n";
            }
        }

        $categoriesMetadata = [
            "categories" => []
        ];
        foreach ($this->categories as $cat) {
            $categoriesMetadata["categories"][] = [
                "code" => $cat['code'],
                "name" => '$translate(name_' . $cat['code'] . ')',
                "description" => '$translate(desc_' . $cat['code'] . ')',
                "nodeCode" => $cat['nodeCode']
            ];
        }

        $metadataTranslations = [];
        foreach ($this->categories as $cat) {
            $metadataTranslations[] = new Translation('name_' . $cat['code'], 'EN', $cat['name_en']);
            $metadataTranslations[] = new Translation('name_' . $cat['code'], 'FR', $cat['name_fr']);
            $metadataTranslations[] = new Translation('name_' . $cat['code'], 'ES', $cat['name_es']);
            $metadataTranslations[] = new Translation('desc_' . $cat['code'], 'EN', $cat['description_en']);
            $metadataTranslations[] = new Translation('desc_' . $cat['code'], 'FR', $cat['description_fr']);
            $metadataTranslations[] = new Translation('desc_' . $cat['code'], 'ES', $cat['description_es']);
        }

        $metadataNode = new ContentNode();
        $metadataNode->parentCode = $this->categoriesContainer->code;
        $metadataNode->type = ContentTypeEnum::JSON;
        $metadataNode->title = "Categories Metadata";
        $metadataNode->code = $this->generateCode('CAT-META');
        $metadataNode->language = "EN";
        $metadataNode->status = StatusEnum::SNAPSHOT;
        $metadataNode->description = "Metadata for all product categories";
        $metadataNode->content = json_encode($categoriesMetadata);
        $metadataNode->translations = $metadataTranslations;

        $this->client->saveContentNode($metadataNode);

        echo "\n";
    }

    public function createStyleNode(): void
    {
        echo "🎨 Creating style node...\n";

        $contentNode = new ContentNode();
        $contentNode->parentCode = $this->siteNode->code;
        $contentNode->type = ContentTypeEnum::STYLE;
        $contentNode->title = "E-Shop Styles";
        $contentNode->code = $this->generateCode('STYLE');
        $contentNode->language = "EN";
        $contentNode->status = StatusEnum::SNAPSHOT;
        $contentNode->description = "CSS styles for the e-commerce shop";
        $contentNode->content = $this->cssContent;

        $this->styleNode = $this->client->saveContentNode($contentNode);
        echo "✅ Style node created: {$this->styleNode->code}\n\n";
    }

    public function createScriptNode(): void
    {
        echo "📜 Creating script node...\n";

        $jsContent = str_replace(
            ['{api_url}', '{core_url}', '{categories_container_code}'],
            [$this->apiUrl, $this->coreUrl, $this->categoriesContainer->code],
            $this->jsContentTemplate
        );

        $contentNode = new ContentNode();
        $contentNode->parentCode = $this->siteNode->code;
        $contentNode->type = ContentTypeEnum::SCRIPT;
        $contentNode->title = "E-Shop Scripts";
        $contentNode->code = $this->generateCode('SCRIPT');
        $contentNode->language = "EN";
        $contentNode->status = StatusEnum::SNAPSHOT;
        $contentNode->description = "JavaScript for the e-commerce shop";
        $contentNode->content = $jsContent;

        $this->scriptNode = $this->client->saveContentNode($contentNode);
        echo "✅ Script node created: {$this->scriptNode->code}\n\n";
    }

    public function createLandingPage(): void
    {
        echo "🏠 Creating landing page...\n";

        $mainTranslations = [
            new Translation("MAIN_TITLE", "EN", "Nodify E-Shop"),
            new Translation("MAIN_TITLE", "FR", "Nodify E-Shop"),
            new Translation("MAIN_TITLE", "ES", "Nodify E-Shop"),
            new Translation("MAIN_SUBTITLE", "EN", "Your one-stop shop for everything"),
            new Translation("MAIN_SUBTITLE", "FR", "Votre boutique en ligne pour tout"),
            new Translation("MAIN_SUBTITLE", "ES", "Tu tienda en línea para todo"),
            new Translation("CART", "EN", "Cart"),
            new Translation("CART", "FR", "Panier"),
            new Translation("CART", "ES", "Carrito"),
            new Translation("SHOP", "EN", "Shop"),
            new Translation("SHOP", "FR", "Boutique"),
            new Translation("SHOP", "ES", "Tienda"),
            new Translation("FOOTER_TEXT", "EN", "Built with ❤️ using Nodify Headless CMS"),
            new Translation("FOOTER_TEXT", "FR", "Construit avec ❤️ en utilisant Nodify Headless CMS"),
            new Translation("FOOTER_TEXT", "ES", "Construido con ❤️ usando Nodify Headless CMS"),
            new Translation("COPYRIGHT", "EN", "© 2026 Nodify E-Shop. All rights reserved."),
            new Translation("COPYRIGHT", "FR", "© 2026 Nodify E-Shop. Tous droits réservés."),
            new Translation("COPYRIGHT", "ES", "© 2026 Nodify E-Shop. Todos los derechos reservados.")
        ];

        $htmlContent = <<<HTML
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nodify E-Shop</title>
        <style>\$content({$this->styleNode->code})</style>
    </head>
    <body>
        <div class="language-selector">
            <select id="langSelect">
                <option value="EN">English</option>
                <option value="FR">Français</option>
                <option value="ES">Español</option>
            </select>
        </div>
        <nav class="navbar">
            <div class="nav-container">
                <div class="logo">🛍️ <span data-translate="MAIN_TITLE">Nodify E-Shop</span></div>
                <ul class="nav-links">
                    <li><a href="#" onclick="loadProducts()" data-translate="SHOP">Shop</a></li>
                    <li><a href="#" onclick="openCart()"><span data-translate="CART">Cart</span> (<span id="cartCount">0</span>)</a></li>
                </ul>
            </div>
        </nav>
        <main class="container">
            <section class="hero">
                <h1 data-translate="MAIN_TITLE">Nodify E-Shop</h1>
                <p data-translate="MAIN_SUBTITLE">Your one-stop shop for everything</p>
            </section>
            <div id="categoryFilters" class="category-filters"></div>
            <div id="productsGrid" class="products-grid"></div>
            <footer class="footer">
                <p data-translate="COPYRIGHT">© 2026 Nodify E-Shop. All rights reserved.</p>
                <p><span data-translate="FOOTER_TEXT">Built with ❤️ using Nodify Headless CMS</span></p>
            </footer>
        </main>

        <div id="productModal" class="modal">
            <div class="modal-content">
                <span class="close" onclick="closeModal()">&times;</span>
                <div id="productModalBody"></div>
            </div>
        </div>

        <div id="cartModal" class="modal">
            <div class="modal-content cart-modal">
                <span class="close" onclick="closeModal()">&times;</span>
                <h2 data-translate="CART">Shopping Cart</h2>
                <div id="cartModalBody"></div>
            </div>
        </div>

        <script>\$content({$this->scriptNode->code})</script>
    </body>
    </html>
    HTML;

        $contentNode = new ContentNode();
        $contentNode->parentCode = $this->siteNode->code;
        $contentNode->type = ContentTypeEnum::HTML;
        $contentNode->title = "E-Shop Landing Page";
        $contentNode->code = $this->generateCode('PAGE');
        $contentNode->language = "EN";
        $contentNode->status = StatusEnum::SNAPSHOT;
        $contentNode->description = "Main landing page for the e-commerce shop";
        $contentNode->content = $htmlContent;
        $contentNode->translations = $mainTranslations;

        $this->landingPage = $this->client->saveContentNode($contentNode);
        echo "✅ Landing page created: {$this->landingPage->code}\n\n";
    }

    public function publishAll(): void
    {
        echo "🚀 Publishing all content...\n";
        $this->client->publishNode($this->siteNode->code);
        echo "✅ Site node published: {$this->siteNode->code}\n";
        echo "   All categories, products, and pages are now published!\n\n";
    }

    public function displayResults(): void
    {
        echo "\n╔════════════════════════════════════════════════════════════╗\n";
        echo "║                    BUILD COMPLETE!                         ║\n";
        echo "╚════════════════════════════════════════════════════════════╝\n\n";

        echo "📊 Site Node: {$this->siteNode->code}\n";
        echo "  ├─ 🎨 Style: {$this->styleNode->code}\n";
        echo "  ├─ 📜 Script: {$this->scriptNode->code}\n";
        echo "  ├─ 📄 Landing Page: {$this->landingPage->code}\n";
        echo "  └─ 📁 Categories Container: {$this->categoriesContainer->code}\n";

        foreach ($this->categories as $cat) {
            echo "      └─ 📁 Category: {$cat['code']} (node: {$cat['nodeCode']})\n";
            $catProducts = array_filter($this->products, function($p) use ($cat) {
                return $p['category_code'] === $cat['code'];
            });
            foreach ($catProducts as $product) {
                echo "          └─ 📦 Product: {$product['title']} (code: {$product['code']})\n";
            }
        }

        echo "\n🌐 Landing Page URL: {$this->coreUrl}/v0/content-node/code/{$this->landingPage->code}\n";
        echo "💡 Change language via the selector at the top right corner (EN, FR, ES)\n";
        echo "💡 Click on any product to view details, add reviews, and add to cart\n";
        echo "💡 Cart is saved in localStorage - persists across page reloads\n\n";
    }

    public function close(): void
    {
        // Nothing to close
    }
}

// Main execution
echo "\n" . str_repeat("=", 60) . "\n";
echo "NODIFY E-COMMERCE EXAMPLE - PHP Client\n";
echo str_repeat("=", 60) . "\n\n";

$builder = new EcommerceBuilder("https://nodify-core.azirar.ovh", "https://nodify-api.azirar.ovh");
try {
    $builder->login();
    $builder->createSiteNode();
    $builder->createCategoriesContainer();
    $builder->createCategoriesAndProducts();
    $builder->createStyleNode();
    $builder->createScriptNode();
    $builder->createLandingPage();
    $builder->publishAll();
    $builder->displayResults();
    $builder->close();
    echo "✅ Done!\n";
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
    exit(1);
}