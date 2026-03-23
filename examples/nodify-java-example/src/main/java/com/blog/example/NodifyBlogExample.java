package com.blog.example;

import com.blog.example.models.Author;
import com.blog.example.models.BlogPost;
import com.blog.example.services.BlogService;
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.ContentTypeEnum;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.Node;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main application demonstrating a blog built with Nodify Java Client
 */
public class NodifyBlogExample {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_URL = "http://localhost:1080";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Admin13579++";
    private static final String BLOG_SITE_NAME = "My Tech Blog";

    public static void main(String[] args) {
        System.out.println("""
                ╔════════════════════════════════════════════════════════════╗
                ║     Nodify Blog Example - Java Client                     ║
                ╚════════════════════════════════════════════════════════════╝
                """);

        ReactiveNodifyClient client = ReactiveNodifyClient.create(
                ReactiveNodifyClient.builder()
                        .withBaseUrl(BASE_URL)
                        .withTimeout(30000)
                        .build()
        );

        CountDownLatch latch = new CountDownLatch(1);

        runBlogExample(client)
                .doFinally(signal -> {
                    if (client instanceof Disposable) {
                        ((Disposable) client).dispose();
                    }
                    latch.countDown();
                })
                .subscribe(
                        result -> System.out.println("\n✅ Blog example completed!"),
                        error -> {
                            System.err.println("\n❌ Error: " + error.getMessage());
                            error.printStackTrace();
                            latch.countDown();
                        }
                );

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Mono<Void> runBlogExample(ReactiveNodifyClient client) {
        AtomicReference<String> siteCode = new AtomicReference<>();
        AtomicReference<String> styleCode = new AtomicReference<>();
        AtomicReference<String> scriptCode = new AtomicReference<>();
        AtomicReference<String> post1Code = new AtomicReference<>();
        AtomicReference<String> post2Code = new AtomicReference<>();
        AtomicReference<String> mainContentCode = new AtomicReference<>();

        return client.login(USERNAME, PASSWORD)
                .flatMap(auth -> {
                    System.out.println("✅ Authenticated successfully");
                    return createBlogSite(client);
                })
                .flatMap(site -> {
                    siteCode.set(site.getCode());
                    System.out.println("📌 Blog site ready: " + site.getName());
                    BlogService blogService = new BlogService(client, siteCode.get());

                    // Create content nodes
                    return createStyleContent(blogService, styleCode)
                            .then(createPost1(blogService, post1Code))
                            .then(createPost2(blogService, post2Code))
                            // Create dynamic script with site code
                            .then(Mono.defer(() ->
                                    createScriptContent(blogService, scriptCode, siteCode.get())))
                            // Create main content with style and script codes
                            .then(Mono.defer(() ->
                                    createMainContent(blogService,
                                            styleCode.get(),
                                            scriptCode.get(),
                                            mainContentCode)))
                            // Add translations
                            .then(Mono.defer(() -> addAllTranslations(blogService,
                                    mainContentCode.get(),
                                    post1Code.get(),
                                    post2Code.get())))
                            // Publish everything
                            .then(Mono.defer(() -> publishAllContent(blogService,
                                    styleCode.get(),
                                    scriptCode.get(),
                                    post1Code.get(),
                                    post2Code.get(),
                                    mainContentCode.get())))
                            // Display final information
                            .then(Mono.defer(() -> displayResults(blogService,
                                    styleCode.get(),
                                    scriptCode.get(),
                                    post1Code.get(),
                                    post2Code.get(),
                                    mainContentCode.get())));
                });
    }

    private static Mono<Node> createBlogSite(ReactiveNodifyClient client) {
        Node site = new Node();
        site.setName(BLOG_SITE_NAME);
        site.setCode("BLOG-SITE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        site.setDescription("my-tech-blog");
        site.setType("SITE");
        site.setStatus(StatusEnum.SNAPSHOT);
        site.setDefaultLanguage("en");
        return client.saveNode(site);
    }

    private static Mono<Void> createStyleContent(BlogService service, AtomicReference<String> codeRef) {
        System.out.println("\n🎨 Creating style content...");

        String css = """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    min-height: 100vh;
                    padding: 40px 20px;
                }
                .container {
                    max-width: 1200px;
                    margin: 0 auto;
                }
                .header {
                    text-align: center;
                    margin-bottom: 40px;
                    padding: 30px;
                    background: white;
                    border-radius: 20px;
                    box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                }
                .header h1 {
                    color: #333;
                    font-size: 2.5em;
                    margin-bottom: 20px;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                }
                .language-selector {
                    margin: 20px 0;
                    padding: 15px;
                    background: #f5f5f5;
                    border-radius: 10px;
                }
                .language-selector select {
                    padding: 10px 20px;
                    border-radius: 25px;
                    border: 2px solid #667eea;
                    font-size: 1em;
                    cursor: pointer;
                }
                .posts-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 30px;
                }
                .post-card {
                    background: white;
                    border-radius: 15px;
                    padding: 25px;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                }
                .post-card h2 {
                    color: #333;
                    margin-bottom: 20px;
                    border-bottom: 3px solid #667eea;
                    padding-bottom: 10px;
                }
                .footer {
                    text-align: center;
                    margin-top: 40px;
                    padding: 20px;
                    color: white;
                }
                """;

        Author author = new Author("Nodify Team", "team@nodify.io",
                "Official Nodify content team", null);

        BlogPost stylePost = new BlogPost(
                null,
                "Blog Styles",
                css,
                "CSS styles for the blog",
                author,
                List.of("css", "styles"),
                "blog-styles",
                "en",
                ContentTypeEnum.STYLE,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createBlogPost(stylePost)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created style content with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createScriptContent(BlogService service,
                                                  AtomicReference<String> codeRef,
                                                  String siteCode) {
        System.out.println("\n📜 Creating dynamic script content...");

        String script = String.format("""
            const BLOG_SITE_CODE = '%s';
            const API_URL = 'https://nodify-api.azirar.ovh';
            
            function changeLanguage(lang) {
                const url = new URL(window.location.href);
                url.searchParams.set('translation', lang);
                window.location.href = url.toString();
            }
            
            async function fetchAllContents() {
                const url = `${API_URL}/contents/node/code/${BLOG_SITE_CODE}?status=SNAPSHOT`;
                try {
                    const response = await fetch(url);
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    return await response.json();
                } catch (error) {
                    console.error('Error fetching contents:', error);
                    return [];
                }
            }
            
            async function loadJsonContent(code, lang = 'en') {
                const url = `${API_URL}/contents/code/${code}?payloadOnly=true&status=SNAPSHOT&translation=${lang}`;
                try {
                    const response = await fetch(url);
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    const data = await response.json();
                    return typeof data === 'string' ? JSON.parse(data) : data;
                } catch (error) {
                    console.error('Error fetching content:', error);
                    return null;
                }
            }
            
                async function loadPosts() {
                     const postsGrid = document.querySelector('.posts-grid');
                     if (!postsGrid) return;
                
                     postsGrid.innerHTML = '';
                
                     const params = new URLSearchParams(window.location.search);
                     const lang = params.get('translation') || 'en';
                
                     // Récupérer tous les contenus du blog
                     const allContents = await fetchAllContents();
                
                     // Filtrer uniquement les contenus de type JSON
                     const jsonPosts = allContents.filter(content => content.type === 'JSON');
                
                     for (const post of jsonPosts) {
                         const postData = await loadJsonContent(post.code, lang);
                         if (!postData) continue;
                
                         const card = document.createElement('div');
                         card.className = 'post-card';
                
                         const title = document.createElement('h2');
                         title.textContent = postData.title || 'Untitled';
                         card.appendChild(title);
                
                         if (postData.description) {
                             const desc = document.createElement('div');
                             // ✅ CORRECTION ICI : innerHTML au lieu de textContent
                             desc.innerHTML = postData.description;
                             card.appendChild(desc);
                         }
                
                         postsGrid.appendChild(card);
                     }
                 }
            
            window.addEventListener('load', function() {
                const params = new URLSearchParams(window.location.search);
                const lang = params.get('translation') || 'en';
                const select = document.querySelector('select');
                if (select) select.value = lang;
                loadPosts();
            });
            """, siteCode);

        Author author = new Author("Nodify Team", "team@nodify.io",
                "Official Nodify content team", null);

        BlogPost scriptPost = new BlogPost(
                null,
                "Blog Dynamic Script",
                script,
                "Dynamic JavaScript that fetches all JSON posts",
                author,
                List.of("js", "javascript", "dynamic"),
                "blog-dynamic-script",
                "en",
                ContentTypeEnum.SCRIPT,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createBlogPost(scriptPost)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created dynamic script content with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createPost1(BlogService service, AtomicReference<String> codeRef) {
        System.out.println("\n📄 Creating post 1 (JSON)...");

        String jsonContent = """
                {
                  "title": "$translate(TITLE)",
                  "description": "$translate(DESCRIPTION)"
                }
                """;

        Author author = new Author("John Doe", "john@example.com",
                "Senior Java Developer", null);

        BlogPost post = new BlogPost(
                null,
                "Presentation of my blog",
                jsonContent,
                "Post 1",
                author,
                List.of("json", "presentation"),
                "post-1",
                "en",
                ContentTypeEnum.JSON,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createBlogPost(post)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created post 1 with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createPost2(BlogService service, AtomicReference<String> codeRef) {
        System.out.println("\n📄 Creating post 2 (JSON with code sample)...");

        String jsonContent = """
            {
              "title": "$translate(TITLE)",
              "description": "<pre><code class=\\\"language-java\\\">\nprivate static final String BASE_URL = \\\"https://nodify-core.azirar.ovh\\\";\nprivate static final String USERNAME = \\\"admin\\\";\nprivate static final String PASSWORD = \\\"Admin13579++\\\";\n\nReactiveNodifyClient client = ReactiveNodifyClient.create(\n    ReactiveNodifyClient.builder()\n        .withBaseUrl(BASE_URL)\n        .withTimeout(30000)\n        .build()\n);\n\nreturn client.login(USERNAME, PASSWORD)\n    .flatMap(auth -> {\n        System.out.println(\\\"✅ Authenticated\\\");\n        return createBlogSite(client);\n    });\n</code></pre>"
            }
            """;

        Author author = new Author("Jane Smith", "jane@example.com",
                "Content strategist", null);

        BlogPost post = new BlogPost(
                null,
                "How to connect to your Nodify using the Java client",
                jsonContent,
                "Post 2 with code example",
                author,
                List.of("json", "code", "java"),
                "post-2",
                "en",
                ContentTypeEnum.JSON,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createBlogPost(post)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created post 2 with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createMainContent(BlogService service,
                                                String styleCode,
                                                String scriptCode,
                                                AtomicReference<String> codeRef) {
        System.out.println("\n📄 Creating main content page...");

        String mainHtml = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Nodify Blog - Main Page</title>
                    <style>
                        $content(%s)
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>$translate(MAIN_TITLE)</h1>
                            <p>$translate(MAIN_DESCRIPTION)</p>
                
                            <div class="language-selector">
                                <select onchange="changeLanguage(this.value)">
                                    <option value="en">English</option>
                                    <option value="fr">Français</option>
                                    <option value="es">Español</option>
                                </select>
                            </div>
                            <p><small>Current language: $translate(CURRENT_LANG)</small></p>
                        </div>
                
                        <div class="posts-grid">
                          <!-- Posts will be loaded dynamically via JavaScript -->
                        </div>
                
                        <div class="footer">
                            <p>$translate(FOOTER_TEXT)</p>
                        </div>
                    </div>
                
                    <script>
                        $content(%s)
                    </script>
                </body>
                </html>
                """, styleCode, scriptCode);

        Author author = new Author("Nodify Team", "team@nodify.io",
                "Official Nodify content team", null);

        BlogPost mainPost = new BlogPost(
                null,
                "Nodify Blog Main Page",
                mainHtml,
                "Main page integrating all blog posts",
                author,
                List.of("blog", "main", "integration"),
                "nodify-blog",
                "en",
                ContentTypeEnum.HTML,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createBlogPost(mainPost)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created main content page with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> addAllTranslations(BlogService service,
                                                 String mainCode,
                                                 String post1Code,
                                                 String post2Code) {
        System.out.println("\n🌍 Adding all translations...");

        return addMainContentTranslations(service, mainCode)
                .then(addJsonPostsTranslations(service, post1Code, post2Code));
    }

    private static Mono<Void> addMainContentTranslations(BlogService service, String mainCode) {
        if (mainCode == null) return Mono.empty();

        // Add translation keys for the main page
        return service.addTranslationKey(mainCode, "en", "MAIN_TITLE", "Example with Nodify")
                .then(service.addTranslationKey(mainCode, "en", "MAIN_DESCRIPTION", "Example of blog with Nodify"))
                .then(service.addTranslationKey(mainCode, "en", "CURRENT_LANG", "English"))
                .then(service.addTranslationKey(mainCode, "en", "FOOTER_TEXT", "Powered by Nodify Headless CMS"))

                .then(service.addTranslationKey(mainCode, "fr", "MAIN_TITLE", "Nodify Blog exemple"))
                .then(service.addTranslationKey(mainCode, "fr", "MAIN_DESCRIPTION", "Exemple de blog avec Nodify"))
                .then(service.addTranslationKey(mainCode, "fr", "CURRENT_LANG", "Français"))
                .then(service.addTranslationKey(mainCode, "fr", "FOOTER_TEXT", "Propulsé par Nodify Headless CMS"))

                .then(service.addTranslationKey(mainCode, "es", "MAIN_TITLE", "Blog con Nodify"))
                .then(service.addTranslationKey(mainCode, "es", "MAIN_DESCRIPTION", "Ejemplo de blog con Nodify"))
                .then(service.addTranslationKey(mainCode, "es", "CURRENT_LANG", "Español"))
                .then(service.addTranslationKey(mainCode, "es", "FOOTER_TEXT", "Desarrollado por Nodify Headless CMS"))
                .then();
    }

    private static Mono<Void> addJsonPostsTranslations(BlogService service,
                                                       String post1Code,
                                                       String post2Code) {
        if (post1Code == null || post2Code == null) return Mono.empty();

        // Add translations for post 1
        return service.addTranslationKey(post1Code, "en", "TITLE", "Presentation of my blog")
                .then(service.addTranslationKey(post1Code, "en", "DESCRIPTION",
                        "This blog is to present the power of Nodify Headless CMS with simple examples."))

                .then(service.addTranslationKey(post1Code, "fr", "TITLE", "Présentation de mon blog"))
                .then(service.addTranslationKey(post1Code, "fr", "DESCRIPTION",
                        "Objectif est de montrer avec des exemples simples la puissance de nodify"))

                .then(service.addTranslationKey(post1Code, "es", "TITLE", "Presentación de mi blog"))
                .then(service.addTranslationKey(post1Code, "es", "DESCRIPTION",
                        "Este blog está creado para presentar el poder de Nodify Headless CMS con ejemplos sencillos."))

                // Add translations for post 2
                .then(service.addTranslationKey(post2Code, "en", "TITLE",
                        "How to connect to your Nodify using the Java client"))

                .then(service.addTranslationKey(post2Code, "fr", "TITLE",
                        "Comment se connecter à votre Nodify en utilisant le client java"))

                .then(service.addTranslationKey(post2Code, "es", "TITLE",
                        "Cómo conectarse a su Nodify usando el cliente Java"))
                .then();
    }

    private static Mono<Void> publishAllContent(BlogService service,
                                                String styleCode,
                                                String scriptCode,
                                                String post1Code,
                                                String post2Code,
                                                String mainCode) {
        System.out.println("\n🚀 Publishing all content...");

        return service.publishBlogPost(styleCode)
                .then(service.publishBlogPost(scriptCode))
                .then(service.publishBlogPost(post1Code))
                .then(service.publishBlogPost(post2Code))
                .then(service.publishBlogPost(mainCode))
                .doOnNext(p -> System.out.println("  ✅ Published: " + p.title()))
                .then();
    }

    private static Mono<Void> displayResults(BlogService service,
                                             String styleCode,
                                             String scriptCode,
                                             String post1Code,
                                             String post2Code,
                                             String mainCode) {
        System.out.println("\n📊 Final Results:");
        System.out.println("  📌 Style content code: " + styleCode);
        System.out.println("  📌 Script content code: " + scriptCode);
        System.out.println("  📌 Post 1 code: " + post1Code);
        System.out.println("  📌 Post 2 code: " + post2Code);
        System.out.println("  📌 Main page code: " + mainCode);

        System.out.println("\n🔍 Access URLs:");
        System.out.println("  Main page: " + BASE_URL + "/v0/content-node/code/" + mainCode);
        System.out.println("  Post 1 (JSON): " + API_URL + "/contents/code/" + post1Code + "?payloadOnly=true&status=SNAPSHOT");
        System.out.println("  Post 2 (JSON): " + API_URL + "/contents/code/" + post2Code + "?payloadOnly=true&status=SNAPSHOT");

        return Mono.empty();
    }
}