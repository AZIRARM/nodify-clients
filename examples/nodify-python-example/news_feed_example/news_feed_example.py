"""
Nodify News Feed Example - Python Client
A dynamic news feed with categories, translations, filtering, and comments
"""

import asyncio
import json
import uuid
import aiohttp
from datetime import datetime
from typing import Optional, Dict, List


class NewsFeedBuilder:
    """Builds a complete news feed structure in Nodify"""

    def __init__(self, core_url: str = "http://localhost:8080", api_url: str = "http://localhost:1080"):
        self.core_url = core_url
        self.api_url = api_url
        self.session: Optional[aiohttp.ClientSession] = None
        self.auth_token: Optional[str] = None

        self.site_node: Optional[Dict] = None
        self.categories_container: Optional[Dict] = None
        self.style_node: Optional[Dict] = None
        self.script_node: Optional[Dict] = None
        self.landing_page: Optional[Dict] = None
        self.categories: List[Dict] = []
        self.articles: List[Dict] = []

        self.css_content = """
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif;
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
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
            border: 1px solid #1e3c72;
            cursor: pointer;
            font-size: 14px;
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
        }
        .logo {
            font-size: 1.5rem;
            font-weight: bold;
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
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
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .hero p {
            font-size: 1.2rem;
            color: #666;
            margin-bottom: 2rem;
        }
        .category-filters {
            display: flex;
            gap: 1rem;
            margin-bottom: 2rem;
            flex-wrap: wrap;
            justify-content: center;
        }
        .category-btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 25px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 500;
            transition: all 0.3s;
            background: rgba(255,255,255,0.9);
            color: #333;
        }
        .category-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }
        .category-btn.active {
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
            color: white;
        }
        .articles-grid {
            display: grid;
            gap: 2rem;
        }
        .article-card {
            background: white;
            border-radius: 15px;
            overflow: hidden;
            transition: transform 0.3s, box-shadow 0.3s;
            cursor: pointer;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
        }
        .article-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }
        .article-content {
            padding: 1.5rem;
        }
        .article-card h3 {
            margin-bottom: 0.5rem;
            color: #333;
            font-size: 1.25rem;
        }
        .article-meta {
            color: #999;
            font-size: 0.85rem;
            margin-bottom: 1rem;
        }
        .comment-count {
            display: inline-block;
            margin-left: 1rem;
            color: #1e3c72;
            font-size: 0.8rem;
            background: #e8f0fe;
            padding: 0.2rem 0.5rem;
            border-radius: 20px;
        }
        .article-category {
            display: inline-block;
            background: #1e3c72;
            color: white;
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.75rem;
            margin-bottom: 0.5rem;
        }
        .read-more {
            display: inline-block;
            margin-top: 1rem;
            color: #1e3c72;
            font-weight: bold;
            cursor: pointer;
        }
        .modal {
            display: none;
            position: fixed;
            z-index: 2000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.8);
            overflow-y: auto;
        }
        .modal-content {
            background: white;
            margin: 50px auto;
            padding: 2rem;
            border-radius: 20px;
            max-width: 800px;
            position: relative;
            animation: slideDown 0.3s ease;
        }
        @keyframes slideDown {
            from { opacity: 0; transform: translateY(-50px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .close {
            position: absolute;
            right: 20px;
            top: 20px;
            font-size: 2rem;
            cursor: pointer;
            color: #999;
            transition: color 0.3s;
        }
        .close:hover {
            color: #333;
        }
        .modal-body {
            margin-top: 1rem;
        }
        .article-full-content {
            line-height: 1.8;
            color: #666;
        }
        .article-full-content h2 {
            color: #1e3c72;
            margin: 1.5rem 0 1rem 0;
        }
        .comment-section {
            margin-top: 2rem;
            padding-top: 1rem;
            border-top: 1px solid #eee;
        }
        .comment-input {
            display: flex;
            gap: 1rem;
            margin-top: 1rem;
        }
        .comment-input input {
            flex: 1;
            padding: 0.75rem;
            border: 1px solid #ddd;
            border-radius: 8px;
        }
        .comment-input input:first-child {
            flex: 0.3;
        }
        .comment-input button {
            padding: 0.75rem 1.5rem;
            background: #1e3c72;
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
        }
        .comment {
            background: #f9f9f9;
            padding: 0.75rem;
            margin: 0.5rem 0;
            border-radius: 8px;
        }
        .comment strong {
            color: #1e3c72;
        }
        .footer {
            background: rgba(255,255,255,0.95);
            text-align: center;
            padding: 2rem;
            margin-top: 4rem;
            border-radius: 20px;
        }
        @media (max-width: 768px) {
            .hero h1 { font-size: 2rem; }
            .hero p { font-size: 1rem; }
            .category-btn { padding: 0.5rem 1rem; font-size: 0.9rem; }
            .comment-input { flex-direction: column; }
            .modal-content { margin: 20px; padding: 1rem; }
        }
        """

        self.js_content_template = """
        const API_BASE_URL = '{api_url}';
        const CORE_BASE_URL = '{core_url}';
        const CATEGORIES_CONTAINER_CODE = '{categories_container_code}';

        let currentModal = null;
        let currentLang = 'EN';
        let currentCategory = null;

        const urlParams = new URLSearchParams(window.location.search);
        const urlLang = urlParams.get('translation');
        if (urlLang && ['EN', 'FR', 'ES'].includes(urlLang)) {{
            currentLang = urlLang;
        }} else if (localStorage.getItem('language')) {{
            currentLang = localStorage.getItem('language');
        }}

        function changeLanguage(lang) {{
            if (!lang || lang === currentLang) return;
            currentLang = lang;
            localStorage.setItem('language', lang);
            const url = new URL(window.location.href);
            url.searchParams.set('translation', lang);
            window.location.href = url.toString();
        }}

        async function fetchCategories() {{
            const url = API_BASE_URL + '/nodes/parent/' + CATEGORIES_CONTAINER_CODE;
            const response = await fetch(url);
            return response.json();
        }}

        async function fetchArticlesFromCategory(categoryCode) {{
            const url = API_BASE_URL + '/contents/node/code/' + categoryCode + '?status=PUBLISHED';
            const response = await fetch(url);
            const articles = await response.json();

            const articlesWithContent = [];
            for (const article of articles) {{
                const contentUrl = API_BASE_URL + '/contents/code/' + article.code + '?payloadOnly=true&status=PUBLISHED&translation=' + currentLang;
                const contentResponse = await fetch(contentUrl);
                if (contentResponse.ok) {{
                    const content = await contentResponse.json();
                    articlesWithContent.push({{
                        code: article.code,
                        title: content.title,
                        summary: content.summary,
                        content: content.content,
                        author: content.author,
                        date: content.date,
                        categoryCode: categoryCode
                    }});
                }}
            }}
            return articlesWithContent;
        }}

        async function getCommentCount(articleCode) {{
            const url = API_BASE_URL + '/datas/content-code/' + articleCode + '/count';
            const response = await fetch(url);
            if (response.ok) {{
                const count = await response.json();
                return count;
            }}
            return 0;
        }}

        async function updateArticleCommentCount(articleCode) {{
            const newCount = await getCommentCount(articleCode);
            const articleCards = document.querySelectorAll('.article-card');
            for (const card of articleCards) {{
                const onclickAttr = card.getAttribute('onclick');
                if (onclickAttr && onclickAttr.includes(articleCode)) {{
                    const commentSpan = card.querySelector('.comment-count');
                    if (commentSpan) {{
                        commentSpan.textContent = `💬 ${{newCount}} comment${{newCount !== 1 ? 's' : ''}}`;
                    }}
                    break;
                }}
            }}
        }}

        async function openArticle(articleCode) {{
            if (!articleCode) {{
                console.error('No article code provided');
                return;
            }}

            const url = API_BASE_URL + '/contents/code/' + articleCode + '?payloadOnly=true&status=PUBLISHED&translation=' + currentLang;
            const response = await fetch(url);
            if (!response.ok) {{
                console.error('Failed to fetch article:', url);
                return;
            }}
            const article = await response.json();

            const modal = document.getElementById('articleModal');
            const modalBody = document.getElementById('modalBody');

            modalBody.innerHTML = `
                <h2>${{escapeHtml(article.title)}}</h2>
                <div class="article-meta">
                    By <strong>${{escapeHtml(article.author)}}</strong> | ${{new Date(article.date).toLocaleDateString()}}
                </div>
                <div class="article-full-content">${{article.content}}</div>
                <div class="comment-section">
                    <h4>Comments</h4>
                    <div id="comments-${{articleCode}}"></div>
                    <div class="comment-input">
                        <input type="text" id="comment-user-${{articleCode}}" placeholder="Your name" value="anonymous">
                        <input type="text" id="comment-text-${{articleCode}}" placeholder="Add a comment...">
                        <button onclick="addComment('${{articleCode}}')">Post</button>
                    </div>
                </div>
            `;

            modal.style.display = 'block';
            currentModal = modal;
            document.body.style.overflow = 'hidden';

            await loadComments(articleCode);
        }}

        async function addComment(articleCode) {{
            const userNameInput = document.getElementById('comment-user-' + articleCode);
            const commentInput = document.getElementById('comment-text-' + articleCode);
            const userName = userNameInput.value.trim() || 'anonymous';
            const commentText = commentInput.value.trim();
            if (!commentText) return;

            const commentData = {{
                user: userName,
                text: commentText,
                date: new Date().toISOString()
            }};

            const url = API_BASE_URL + '/datas/';
            const response = await fetch(url, {{
                method: 'POST',
                headers: {{ 'Content-Type': 'application/json' }},
                body: JSON.stringify({{
                    contentNodeCode: articleCode,
                    dataType: 'COMMENT',
                    name: 'Comment',
                    user: userName,
                    key: 'comment_' + Date.now(),
                    value: JSON.stringify(commentData)
                }})
            }});

            if (response.ok) {{
                commentInput.value = '';
                await loadComments(articleCode);
                await updateArticleCommentCount(articleCode);
            }}
        }}

        async function loadComments(articleCode) {{
            const url = API_BASE_URL + '/datas/contentCode/' + articleCode + '?limit=50';
            const response = await fetch(url);
            const comments = await response.json();

            const commentsDiv = document.getElementById('comments-' + articleCode);
            if (commentsDiv) {{
                if (comments.length === 0) {{
                    commentsDiv.innerHTML = '<p style="color: #999;">No comments yet. Be the first to comment!</p>';
                }} else {{
                    commentsDiv.innerHTML = comments.map(c => {{
                        let commentData;
                        try {{
                            commentData = JSON.parse(c.value);
                        }} catch(e) {{
                            commentData = {{ user: c.user || 'anonymous', text: c.value }};
                        }}
                        return `
                            <div class="comment">
                                <strong>${{escapeHtml(commentData.user)}}</strong>
                                <span style="color: #999; font-size: 0.8rem;">${{commentData.date ? new Date(commentData.date).toLocaleString() : ''}}</span>
                                <p>${{escapeHtml(commentData.text)}}</p>
                            </div>
                        `;
                    }}).join('');
                }}
            }}
        }}

        function escapeHtml(text) {{
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }}

        function closeModal() {{
            if (currentModal) {{
                currentModal.style.display = 'none';
                currentModal = null;
                document.body.style.overflow = 'auto';
            }}
        }}

        function updateStaticTranslations() {{
            const elements = document.querySelectorAll('[data-translate]');
            const translations = {{
                EN: {{
                    MAIN_TITLE: 'Nodify News Feed',
                    MAIN_SUBTITLE: 'Stay updated with the latest news',
                    COPYRIGHT: '© 2026 Nodify CMS. All rights reserved.',
                    FOOTER_TEXT: 'Built with ❤️ using Nodify Headless CMS'
                }},
                FR: {{
                    MAIN_TITLE: 'Flux d\\'actualités Nodify',
                    MAIN_SUBTITLE: 'Restez informé des dernières actualités',
                    COPYRIGHT: '© 2026 Nodify CMS. Tous droits réservés.',
                    FOOTER_TEXT: 'Construit avec ❤️ en utilisant Nodify Headless CMS'
                }},
                ES: {{
                    MAIN_TITLE: 'Feed de Noticias Nodify',
                    MAIN_SUBTITLE: 'Manténgase actualizado con las últimas noticias',
                    COPYRIGHT: '© 2026 Nodify CMS. Todos los derechos reservados.',
                    FOOTER_TEXT: 'Construido con ❤️ usando Nodify Headless CMS'
                }}
            }};
            elements.forEach(el => {{
                const key = el.getAttribute('data-translate');
                if (translations[currentLang] && translations[currentLang][key]) {{
                    el.textContent = translations[currentLang][key];
                }}
            }});
        }}

        async function loadArticles() {{
            const grid = document.getElementById('articlesGrid');
            if (!grid) return;

            try {{
                const categories = await fetchCategories();

                if (!categories || categories.length === 0) {{
                    grid.innerHTML = '<p style="color: white; text-align: center;">No categories found.</p>';
                    return;
                }}

                const filterContainer = document.getElementById('categoryFilters');
                filterContainer.innerHTML = `
                    <button class="category-btn ${{!currentCategory ? 'active' : ''}}" onclick="filterByCategory(null)">All</button>
                    ${{categories.map(cat => `
                        <button class="category-btn ${{currentCategory === cat.code ? 'active' : ''}}" onclick="filterByCategory('${{cat.code}}')">
                            ${{escapeHtml(cat.name)}}
                        </button>
                    `).join('')}}
                `;

                let allArticles = [];
                for (const cat of categories) {{
                    const articles = await fetchArticlesFromCategory(cat.code);
                    if (articles && articles.length > 0) {{
                        for (const article of articles) {{
                            const commentCount = await getCommentCount(article.code);
                            allArticles.push({{
                                code: article.code,
                                title: article.title,
                                summary: article.summary,
                                content: article.content,
                                author: article.author,
                                date: article.date,
                                categoryName: cat.name,
                                categoryCode: cat.code,
                                commentCount: commentCount
                            }});
                        }}
                    }}
                }}

                if (currentCategory) {{
                    allArticles = allArticles.filter(a => a.categoryCode === currentCategory);
                }}

                if (allArticles.length === 0) {{
                    grid.innerHTML = '<p style="color: white; text-align: center;">No articles found.</p>';
                    return;
                }}

                grid.innerHTML = allArticles.map(article => `
                    <div class="article-card" onclick="openArticle('${{article.code}}')">
                        <div class="article-content">
                            <span class="article-category">${{escapeHtml(article.categoryName)}}</span>
                            <h3>${{escapeHtml(article.title)}}</h3>
                            <div class="article-meta">
                                By <strong>${{escapeHtml(article.author)}}</strong> | ${{new Date(article.date).toLocaleDateString()}}
                                <span class="comment-count">💬 ${{article.commentCount}} comment${{article.commentCount !== 1 ? 's' : ''}}</span>
                            </div>
                            <p>${{escapeHtml(article.summary?.substring(0, 150))}}...</p>
                            <span class="read-more">Read More →</span>
                        </div>
                    </div>
                `).join('');
            }} catch (error) {{
                console.error('Error loading articles:', error);
                grid.innerHTML = '<p style="color: white; text-align: center;">Error loading articles.</p>';
            }}
        }}

        function filterByCategory(categoryCode) {{
            currentCategory = categoryCode;
            loadArticles();
        }}

        document.addEventListener('keydown', (e) => {{
            if (e.key === 'Escape') closeModal();
        }});

        window.onclick = (e) => {{
            if (currentModal && e.target === currentModal) closeModal();
        }};

        document.addEventListener('DOMContentLoaded', async () => {{
            updateStaticTranslations();
            await loadArticles();

            const select = document.getElementById('langSelect');
            if (select) {{
                select.value = currentLang;
                select.onchange = (e) => changeLanguage(e.target.value);
            }}
        }});
        """

    async def _get_session(self):
        if self.session is None or self.session.closed:
            self.session = aiohttp.ClientSession()
        return self.session

    async def _request(self, method: str, url: str, data: Optional[Dict] = None, auth: bool = True):
        session = await self._get_session()
        headers = {"Content-Type": "application/json"}
        if auth and self.auth_token:
            headers["Authorization"] = f"Bearer {self.auth_token}"

        async with session.request(method, url, json=data, headers=headers) as resp:
            if resp.status >= 400:
                text = await resp.text()
                raise Exception(f"Request failed: {resp.status} - {text[:200]}")
            if resp.status == 204:
                return None
            return await resp.json()

    async def login(self):
        print("🔐 Authenticating...")
        url = f"{self.core_url}/authentication/login"
        data = {"email": "admin", "password": "Admin13579++"}

        session = await self._get_session()
        async with session.post(url, json=data) as resp:
            if resp.status != 200:
                text = await resp.text()
                raise Exception(f"Login failed: {resp.status} - {text}")
            result = await resp.json()
            self.auth_token = result.get("token")

        print("✅ Authenticated successfully\n")
        return self

    async def create_site_node(self):
        print("📁 Creating main site node...")
        node_data = {
            "name": "Nodify News Feed",
            "code": f"SITE-{uuid.uuid4().hex[:12].upper()}",
            "type": "SITE",
            "status": "SNAPSHOT",
            "defaultLanguage": "EN",
            "description": "A dynamic news feed built with Nodify Python client",
            "languages": ["EN", "FR", "ES"]
        }
        url = f"{self.core_url}/v0/nodes/"
        self.site_node = await self._request("POST", url, node_data)
        print(f"✅ Site node created: {self.site_node['code']}\n")
        return self

    async def create_categories_container(self):
        print("📁 Creating categories container node...")
        node_data = {
            "parentCode": self.site_node["code"],
            "name": "Categories",
            "code": f"CATEGORIES-{uuid.uuid4().hex[:12].upper()}",
            "type": "FOLDER",
            "status": "SNAPSHOT",
            "description": "Container for all news categories"
        }
        url = f"{self.core_url}/v0/nodes/"
        self.categories_container = await self._request("POST", url, node_data)
        print(f"✅ Categories container created: {self.categories_container['code']}\n")
        return self

    async def create_categories_and_articles(self):
        print("📋 Creating categories and articles...")

        categories_data = [
            {"code": "TECH", "name": "Technology", "description": "Latest in technology and innovation"},
            {"code": "SPORTS", "name": "Sports", "description": "Sports news and updates"},
            {"code": "POLITICS", "name": "Politics", "description": "Political news and analysis"}
        ]

        articles_with_translations = {
            "TECH": [
                {
                    "title_en": "AI Revolution: How Machine Learning is Changing Everything",
                    "title_fr": "Révolution IA : Comment le Machine Learning change tout",
                    "title_es": "Revolución IA: Cómo el Machine Learning lo está cambiando todo",
                    "summary_en": "Discover how artificial intelligence is transforming industries worldwide",
                    "summary_fr": "Découvrez comment l'intelligence artificielle transforme les industries dans le monde",
                    "summary_es": "Descubre cómo la inteligencia artificial está transformando industrias en todo el mundo",
                    "content_en": "<h2>The AI Revolution</h2><p>Artificial intelligence is no longer science fiction. From healthcare to finance, AI is transforming how we work and live.</p><p>The future is here, and it's powered by artificial intelligence.</p>",
                    "content_fr": "<h2>La Révolution IA</h2><p>L'intelligence artificielle n'est plus de la science-fiction. De la santé à la finance, l'IA transforme notre façon de travailler et de vivre.</p><p>L'avenir est là, et il est alimenté par l'intelligence artificielle.</p>",
                    "content_es": "<h2>La Revolución IA</h2><p>La inteligencia artificial ya no es ciencia ficción. Desde la salud hasta las finanzas, la IA está transformando cómo trabajamos y vivimos.</p><p>El futuro está aquí y está impulsado por la inteligencia artificial.</p>",
                    "author": "Sarah Johnson",
                    "date": datetime.now().isoformat()
                },
                {
                    "title_en": "Kubernetes at Scale: Managing Cloud Infrastructure",
                    "title_fr": "Kubernetes à grande échelle : Gérer l'infrastructure cloud",
                    "title_es": "Kubernetes a escala: Gestionando infraestructura cloud",
                    "summary_en": "Best practices for running Kubernetes in production environments",
                    "summary_fr": "Bonnes pratiques pour exécuter Kubernetes en production",
                    "summary_es": "Mejores prácticas para ejecutar Kubernetes en entornos de producción",
                    "content_en": "<h2>Kubernetes at Scale</h2><p>Running Kubernetes in production requires careful planning and best practices.</p><p>With the right approach, Kubernetes can power any workload.</p>",
                    "content_fr": "<h2>Kubernetes à grande échelle</h2><p>Exécuter Kubernetes en production nécessite une planification minutieuse et des bonnes pratiques.</p><p>Avec la bonne approche, Kubernetes peut alimenter n'importe quelle charge de travail.</p>",
                    "content_es": "<h2>Kubernetes a escala</h2><p>Ejecutar Kubernetes en producción requiere una planificación cuidadosa y mejores prácticas.</p><p>Con el enfoque correcto, Kubernetes puede manejar cualquier carga de trabajo.</p>",
                    "author": "Michael Chen",
                    "date": datetime.now().isoformat()
                }
            ],
            "SPORTS": [
                {
                    "title_en": "Champions League Final: Historic Comeback",
                    "title_fr": "Finale de la Ligue des Champions : Retour historique",
                    "title_es": "Final de la Champions League: Remontada histórica",
                    "summary_en": "Dramatic finish in the Champions League final",
                    "summary_fr": "Fin dramatique en finale de la Ligue des Champions",
                    "summary_es": "Final dramático en la final de la Champions League",
                    "content_en": "<h2>Historic Comeback</h2><p>In what will be remembered as one of the greatest finals in history, the underdogs completed an incredible comeback from 3-0 down to win 4-3 in extra time.</p><p>This victory will be talked about for generations.</p>",
                    "content_fr": "<h2>Retour historique</h2><p>Dans ce qui restera comme l'une des plus grandes finales de l'histoire, les outsiders ont réalisé un incroyable retour de 3-0 pour gagner 4-3 en prolongation.</p><p>Cette victoire fera parler d'elle pendant des générations.</p>",
                    "content_es": "<h2>Remontada histórica</h2><p>En lo que será recordada como una de las mejores finales de la historia, los underdogs completaron una increíble remontada de 3-0 para ganar 4-3 en tiempo extra.</p><p>Esta victoria será recordada por generaciones.</p>",
                    "author": "James Wilson",
                    "date": datetime.now().isoformat()
                }
            ],
            "POLITICS": [
                {
                    "title_en": "Election Results: A New Era Begins",
                    "title_fr": "Résultats des élections : Une nouvelle ère commence",
                    "title_es": "Resultados electorales: Comienza una nueva era",
                    "summary_en": "Landmark election brings historic changes",
                    "summary_fr": "Une élection historique apporte des changements majeurs",
                    "summary_es": "Elección histórica trae cambios importantes",
                    "content_en": "<h2>A New Era</h2><p>The election results mark a turning point in the nation's history. With record voter turnout, the people have spoken clearly for change.</p><p>Analysts predict significant policy shifts in the coming months.</p>",
                    "content_fr": "<h2>Une nouvelle ère</h2><p>Les résultats des élections marquent un tournant dans l'histoire de la nation. Avec un taux de participation record, le peuple s'est clairement exprimé pour le changement.</p><p>Les analystes prévoient des changements politiques majeurs dans les mois à venir.</p>",
                    "content_es": "<h2>Una nueva era</h2><p>Los resultados electorales marcan un punto de inflexión en la historia de la nación. Con una participación récord, el pueblo ha hablado claramente por el cambio.</p><p>Los analistas predicen cambios políticos significativos en los próximos meses.</p>",
                    "author": "Emma Thompson",
                    "date": datetime.now().isoformat()
                }
            ]
        }

        for cat_data in categories_data:
            category_node_data = {
                "parentCode": self.categories_container["code"],
                "name": cat_data["name"],
                "code": f"CAT-{uuid.uuid4().hex[:12].upper()}",
                "type": "FOLDER",
                "status": "SNAPSHOT",
                "description": cat_data["description"]
            }
            url = f"{self.core_url}/v0/nodes/"
            category_node = await self._request("POST", url, category_node_data)

            self.categories.append({
                "code": cat_data["code"],
                "name": cat_data["name"],
                "node_code": category_node["code"]
            })
            print(f"  ✅ Category created: {cat_data['code']} (node: {category_node['code']})")

            cat_articles = articles_with_translations.get(cat_data["code"], [])
            for trans in cat_articles:
                article_code = f"POST-{uuid.uuid4().hex[:12].upper()}"

                article_payload = {
                    "title": "$translate(title)",
                    "summary": "$translate(summary)",
                    "content": "$translate(content)",
                    "author": trans["author"],
                    "date": trans["date"],
                    "category_code": cat_data["code"]
                }

                translations_list = [
                    {"language": "EN", "key": "title", "value": trans["title_en"]},
                    {"language": "EN", "key": "summary", "value": trans["summary_en"]},
                    {"language": "EN", "key": "content", "value": trans["content_en"]},
                    {"language": "FR", "key": "title", "value": trans["title_fr"]},
                    {"language": "FR", "key": "summary", "value": trans["summary_fr"]},
                    {"language": "FR", "key": "content", "value": trans["content_fr"]},
                    {"language": "ES", "key": "title", "value": trans["title_es"]},
                    {"language": "ES", "key": "summary", "value": trans["summary_es"]},
                    {"language": "ES", "key": "content", "value": trans["content_es"]}
                ]

                content_node_data = {
                    "parentCode": category_node["code"],
                    "type": "JSON",
                    "title": trans["title_en"],
                    "code": article_code,
                    "language": "EN",
                    "status": "SNAPSHOT",
                    "environmentCode": "production",
                    "description": trans["summary_en"],
                    "content": json.dumps(article_payload),
                    "translations": translations_list
                }

                content_url = f"{self.core_url}/v0/content-node/"
                await self._request("POST", content_url, content_node_data)
                print(f"      ✅ Article created: {article_code}")

                self.articles.append({
                    "code": article_code,
                    "title": trans["title_en"],
                    "category_code": cat_data["code"]
                })

        print("")
        return self

    async def create_style_node(self):
        print("🎨 Creating style node...")
        content_node_data = {
            "parentCode": self.site_node["code"],
            "type": "STYLE",
            "title": "News Feed Styles",
            "code": f"STYLE-{uuid.uuid4().hex[:12].upper()}",
            "language": "EN",
            "status": "SNAPSHOT",
            "environmentCode": "production",
            "description": "CSS styles for the news feed",
            "content": self.css_content
        }
        url = f"{self.core_url}/v0/content-node/"
        self.style_node = await self._request("POST", url, content_node_data)
        print(f"✅ Style node created: {self.style_node['code']}\n")
        return self

    async def create_script_node(self):
        print("📜 Creating script node...")

        js_content = self.js_content_template.format(
            api_url=self.api_url,
            core_url=self.core_url,
            categories_container_code=self.categories_container["code"]
        )

        content_node_data = {
            "parentCode": self.site_node["code"],
            "type": "SCRIPT",
            "title": "News Feed Scripts",
            "code": f"SCRIPT-{uuid.uuid4().hex[:12].upper()}",
            "language": "EN",
            "status": "SNAPSHOT",
            "environmentCode": "production",
            "description": "JavaScript for the news feed",
            "content": js_content
        }

        url = f"{self.core_url}/v0/content-node/"
        self.script_node = await self._request("POST", url, content_node_data)
        print(f"✅ Script node created: {self.script_node['code']}\n")
        return self

    async def create_landing_page(self):
        print("🏠 Creating landing page...")

        main_translations = [
            {"language": "EN", "key": "MAIN_TITLE", "value": "Nodify News Feed"},
            {"language": "EN", "key": "MAIN_SUBTITLE", "value": "Stay updated with the latest news"},
            {"language": "EN", "key": "FOOTER_TEXT", "value": "Built with ❤️ using Nodify Headless CMS"},
            {"language": "EN", "key": "COPYRIGHT", "value": "© 2026 Nodify CMS. All rights reserved."},
            {"language": "FR", "key": "MAIN_TITLE", "value": "Flux d'actualités Nodify"},
            {"language": "FR", "key": "MAIN_SUBTITLE", "value": "Restez informé des dernières actualités"},
            {"language": "FR", "key": "FOOTER_TEXT", "value": "Construit avec ❤️ en utilisant Nodify Headless CMS"},
            {"language": "FR", "key": "COPYRIGHT", "value": "© 2026 Nodify CMS. Tous droits réservés."},
            {"language": "ES", "key": "MAIN_TITLE", "value": "Feed de Noticias Nodify"},
            {"language": "ES", "key": "MAIN_SUBTITLE", "value": "Manténgase actualizado con las últimas noticias"},
            {"language": "ES", "key": "FOOTER_TEXT", "value": "Construido con ❤️ usando Nodify Headless CMS"},
            {"language": "ES", "key": "COPYRIGHT", "value": "© 2026 Nodify CMS. Todos los derechos reservados."}
        ]

        html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nodify News Feed</title>
    <style>$content({self.style_node['code']})</style>
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
            <div class="logo">📰 <span data-translate="MAIN_TITLE">Nodify News Feed</span></div>
        </div>
    </nav>
    <main class="container">
        <section class="hero">
            <h1 data-translate="MAIN_TITLE">Nodify News Feed</h1>
            <p data-translate="MAIN_SUBTITLE">Stay updated with the latest news</p>
        </section>
        <div id="categoryFilters" class="category-filters"></div>
        <div id="articlesGrid" class="articles-grid"></div>
        <footer class="footer">
            <p data-translate="COPYRIGHT">© 2026 Nodify CMS. All rights reserved.</p>
            <p><span data-translate="FOOTER_TEXT">Built with ❤️ using Nodify Headless CMS</span></p>
        </footer>
    </main>
    <div id="articleModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <div id="modalBody"></div>
        </div>
    </div>
    <script>$content({self.script_node['code']})</script>
</body>
</html>"""

        content_node_data = {
            "parentCode": self.site_node["code"],
            "type": "HTML",
            "title": "Nodify News Feed Landing Page",
            "code": f"PAGE-{uuid.uuid4().hex[:12].upper()}",
            "language": "EN",
            "status": "SNAPSHOT",
            "environmentCode": "production",
            "description": "Main landing page for the news feed",
            "content": html_content,
            "translations": main_translations
        }

        url = f"{self.core_url}/v0/content-node/"
        self.landing_page = await self._request("POST", url, content_node_data)
        print(f"✅ Landing page created: {self.landing_page['code']}\n")
        return self

    async def publish_all(self):
        print("🚀 Publishing all content...")
        url = f"{self.core_url}/v0/nodes/code/{self.site_node['code']}/publish"
        await self._request("POST", url, None)
        print(f"✅ Site node published: {self.site_node['code']}\n")
        return self

    def display_results(self):
        print("\n╔════════════════════════════════════════════════════════════╗")
        print("║                    BUILD COMPLETE!                         ║")
        print("╚════════════════════════════════════════════════════════════╝\n")
        print(f"📊 Site Node: {self.site_node['code']}")
        print(f"  ├─ 🎨 Style: {self.style_node['code']}")
        print(f"  ├─ 📜 Script: {self.script_node['code']}")
        print(f"  ├─ 📄 Landing Page: {self.landing_page['code']}")
        print(f"  └─ 📁 Categories Container: {self.categories_container['code']}")
        for cat in self.categories:
            print(f"      └─ 📁 Category: {cat['code']} (node: {cat['node_code']})")
            cat_articles = [a for a in self.articles if a['category_code'] == cat['code']]
            for article in cat_articles:
                print(f"          └─ 📝 {article['code']}")
        print(f"\n🌐 Landing Page URL: {self.core_url}/v0/content-node/code/{self.landing_page['code']}")
        print("💡 Change language via the selector at the top right corner (EN, FR, ES)\n")

    async def close(self):
        if self.session and not self.session.closed:
            await self.session.close()


async def main():
    print("\n" + "="*60)
    print("NODIFY NEWS FEED EXAMPLE - Python Client")
    print("="*60 + "\n")
    builder = NewsFeedBuilder()
    await builder.login()
    await builder.create_site_node()
    await builder.create_categories_container()
    await builder.create_categories_and_articles()
    await builder.create_style_node()
    await builder.create_script_node()
    await builder.create_landing_page()
    await builder.publish_all()
    builder.display_results()
    await builder.close()
    print("✅ Done!")


if __name__ == "__main__":
    asyncio.run(main())