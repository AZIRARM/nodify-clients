package com.blog.example;

import com.blog.example.models.Author;
import com.blog.example.models.BlogPost;
import com.blog.example.services.BlogService;
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.Node;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class NodifyBlogExample {

    private static final String BASE_URL = "https://nodify-core.azirar.ovh";
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
        AtomicReference<String> post1Code = new AtomicReference<>();
        AtomicReference<String> post2Code = new AtomicReference<>();
        AtomicReference<String> mainContentCode = new AtomicReference<>();

        return client.login(USERNAME, PASSWORD)
                .flatMap(auth -> {
                    System.out.println("✅ Authenticated");
                    return createBlogSite(client);
                })
                .flatMap(site -> {
                    siteCode.set(site.getCode());
                    System.out.println("📌 Blog site ready: " + site.getName());
                    BlogService blogService = new BlogService(client, siteCode.get());

                    return createPost1(blogService, post1Code)
                            .then(createPost2(blogService, post2Code))
                            .then(Mono.defer(() -> addTranslations(blogService, post1Code.get(), post2Code.get())))
                            .then(Mono.defer(() -> createMainContent(blogService, post1Code.get(), post2Code.get(), mainContentCode)))
                            .then(Mono.defer(() -> addMainContentTranslations(blogService,
                                    mainContentCode.get(), post1Code.get(), post2Code.get())))
                            .then(Mono.defer(() -> publishPosts(blogService,
                                    post1Code.get(), post2Code.get(), mainContentCode.get())))
                            .then(Mono.defer(() -> viewPosts(blogService,
                                    post1Code.get(), post2Code.get(), mainContentCode.get())));
                });
    }

    private static Mono<Node> createBlogSite(ReactiveNodifyClient client) {
        Node site = new Node();
        site.setName(BLOG_SITE_NAME);
        site.setCode("BLOG-SITE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        site.setSlug("my-tech-blog");
        site.setType("SITE");
        site.setStatus(StatusEnum.SNAPSHOT);
        site.setDefaultLanguage("en");
        return client.saveNode(site);
    }

    private static Mono<Void> createPost1(BlogService service, AtomicReference<String> codeRef) {
        Author author = new Author("John Doe", "john@example.com",
                "Senior Java Developer", null);

        BlogPost post = new BlogPost(null,
                "Getting Started with Nodify",
                "<h1>$translate(TITLE)</h1><p>Welcome to Nodify Java client.</p>",
                "Learn Nodify basics",
                author, List.of("java"), "getting-started", "en",
                Instant.now(), Instant.now(), false);

        return service.createBlogPost(post)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created: " + saved.title() + " (Code: " + codeRef.get() + ")");
                })
                .then();
    }

    private static Mono<Void> createPost2(BlogService service, AtomicReference<String> codeRef) {
        Author author = new Author("Jane Smith", "jane@example.com",
                "Content strategist", null);

        BlogPost post = new BlogPost(null,
                "Multi-language Content",
                "<h1>$translate(TITLE)</h1><p>Managing content in multiple languages.</p>",
                "Multi-language guide",
                author, List.of("i18n"), "multi-language", "en",
                Instant.now(), Instant.now(), false);

        return service.createBlogPost(post)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created: " + saved.title() + " (Code: " + codeRef.get() + ")");
                })
                .then();
    }

    private static Mono<Void> addTranslations(BlogService service, String code1, String code2) {
        if (code1 == null || code2 == null) return Mono.empty();

        System.out.println("\n🌍 Adding translations...");
        return service.addTranslation(code1, "fr", "Premiers pas avec Nodify",
                        "<h1>$translate(TITLE)</h1><p>Bienvenue sur le client Java Nodify.</p>")
                .then(service.addTranslation(code2, "es", "Contenido multilingüe",
                        "<h1>$translate(TITLE)</h1><p>Gestión de contenido en múltiples idiomas.</p>"))
                .then();
    }

    private static Mono<Void> createMainContent(BlogService service, String post1Code, String post2Code,
                                                AtomicReference<String> codeRef) {
        System.out.println("\n📄 Creating main content page...");

        Author author = new Author("Nodify Team", "team@nodify.io",
                "Official Nodify content team", null);

        String mainHtml = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nodify Blog - Main Page</title>
                <style>
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
                        <div class="post-card">
                            <h2>$translate(POST1_TITLE)</h2>
                            $content(POST1_CODE)
                        </div>
                        <div class="post-card">
                            <h2>$translate(POST2_TITLE)</h2>
                            $content(POST2_CODE)
                        </div>
                    </div>

                    <div class="footer">
                        <p>$translate(FOOTER_TEXT)</p>
                    </div>
                </div>

                <script>
                    function changeLanguage(lang) {
                        const url = new URL(window.location.href);
                        url.searchParams.set('translation', lang);
                        window.location.href = url.toString();
                    }

                    window.addEventListener('load', function() {
                        const params = new URLSearchParams(window.location.search);
                        const lang = params.get('translation') || 'en';
                        document.querySelector('select').value = lang;
                    });
                </script>
            </body>
            </html>
            """;

        String finalHtml = mainHtml
                .replace("POST1_CODE", post1Code)
                .replace("POST2_CODE", post2Code);

        BlogPost mainPost = new BlogPost(null,
                "Nodify Blog Main Page",
                finalHtml,
                "Main page integrating all blog posts",
                author,
                List.of("blog", "main", "integration"),
                "nodify-blog",
                "en",
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

    private static Mono<Void> addMainContentTranslations(BlogService service, String mainCode,
                                                         String post1Code, String post2Code) {
        if (mainCode == null || post1Code == null || post2Code == null) return Mono.empty();

        System.out.println("\n🌍 Adding main content translations...");

        String frenchHtml = """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Blog Nodify - Page Principale</title>
            <style>
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
                    <p><small>Langue actuelle: $translate(CURRENT_LANG)</small></p>
                </div>

                <div class="posts-grid">
                    <div class="post-card">
                        <h2>$translate(POST1_TITLE)</h2>
                        $content(POST1_CODE)
                    </div>
                    <div class="post-card">
                        <h2>$translate(POST2_TITLE)</h2>
                        $content(POST2_CODE)
                    </div>
                </div>

                <div class="footer">
                    <p>$translate(FOOTER_TEXT)</p>
                </div>
            </div>

            <script>
                function changeLanguage(lang) {
                    const url = new URL(window.location.href);
                    url.searchParams.set('translation', lang);
                    window.location.href = url.toString();
                }

                window.addEventListener('load', function() {
                    const params = new URLSearchParams(window.location.search);
                    const lang = params.get('translation') || 'en';
                    document.querySelector('select').value = lang;
                });
            </script>
        </body>
        </html>
        """;

        String spanishHtml = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Blog Nodify - Página Principal</title>
            <style>
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
                    <p><small>Idioma actual: $translate(CURRENT_LANG)</small></p>
                </div>

                <div class="posts-grid">
                    <div class="post-card">
                        <h2>$translate(POST1_TITLE)</h2>
                        $content(POST1_CODE)
                    </div>
                    <div class="post-card">
                        <h2>$translate(POST2_TITLE)</h2>
                        $content(POST2_CODE)
                    </div>
                </div>

                <div class="footer">
                    <p>$translate(FOOTER_TEXT)</p>
                </div>
            </div>

            <script>
                function changeLanguage(lang) {
                    const url = new URL(window.location.href);
                    url.searchParams.set('translation', lang);
                    window.location.href = url.toString();
                }

                window.addEventListener('load', function() {
                    const params = new URLSearchParams(window.location.search);
                    const lang = params.get('translation') || 'en';
                    document.querySelector('select').value = lang;
                });
            </script>
        </body>
        </html>
        """;

        String finalFrenchHtml = frenchHtml
                .replace("POST1_CODE", post1Code)
                .replace("POST2_CODE", post2Code);

        String finalSpanishHtml = spanishHtml
                .replace("POST1_CODE", post1Code)
                .replace("POST2_CODE", post2Code);

        return service.addTranslation(mainCode, "fr",
                        "Page principale du blog Nodify", finalFrenchHtml)
                .then(service.addTranslation(mainCode, "es",
                        "Página principal del blog Nodify", finalSpanishHtml))
                // Ajouter les traductions pour les clés utilisées dans les directives $translate
                .then(service.addTranslation(mainCode, "fr", "MAIN_TITLE", "Blog Nodify - Page Principale"))
                .then(service.addTranslation(mainCode, "es", "MAIN_TITLE", "Blog Nodify - Página Principal"))

                .then(service.addTranslation(mainCode, "fr", "MAIN_DESCRIPTION",
                        "Page principale intégrant tous les articles du blog"))
                .then(service.addTranslation(mainCode, "es", "MAIN_DESCRIPTION",
                        "Página principal que integra todas las publicaciones del blog"))

                .then(service.addTranslation(mainCode, "fr", "POST1_TITLE",
                        "Premiers pas avec Nodify"))
                .then(service.addTranslation(mainCode, "es", "POST1_TITLE",
                        "Primeros pasos con Nodify"))

                .then(service.addTranslation(mainCode, "fr", "POST2_TITLE",
                        "Contenu multilingue"))
                .then(service.addTranslation(mainCode, "es", "POST2_TITLE",
                        "Contenido multilingüe"))

                .then(service.addTranslation(mainCode, "fr", "FOOTER_TEXT",
                        "Propulsé par Nodify Headless CMS"))
                .then(service.addTranslation(mainCode, "es", "FOOTER_TEXT",
                        "Desarrollado por Nodify Headless CMS"))

                .then(service.addTranslation(mainCode, "fr", "CURRENT_LANG", "Français"))
                .then(service.addTranslation(mainCode, "es", "CURRENT_LANG", "Español"))
                .then();
    }

    private static Mono<Void> publishPosts(BlogService service, String code1, String code2, String mainCode) {
        if (code1 == null || code2 == null || mainCode == null) return Mono.empty();

        System.out.println("\n🚀 Publishing posts...");
        return service.publishBlogPost(code1)
                .then(service.publishBlogPost(code2))
                .then(service.publishBlogPost(mainCode))
                .doOnNext(p -> System.out.println("  ✅ Published: " + p.title()))
                .then();
    }

    private static Mono<Void> viewPosts(BlogService service, String code1, String code2, String mainCode) {
        if (code1 == null || code2 == null || mainCode == null) return Mono.empty();

        System.out.println("\n📖 Published content:");
        return service.findBlogPostByCode(mainCode)
                .doOnNext(p -> System.out.println("\n  📌 MAIN: " + p.title()))
                .then(service.findBlogPostByCode(code1))
                .doOnNext(p -> System.out.println("  📌 POST 1: " + p.title()))
                .then(service.findBlogPostByCode(code2))
                .doOnNext(p -> System.out.println("  📌 POST 2: " + p.title()))
                .then();
    }
}