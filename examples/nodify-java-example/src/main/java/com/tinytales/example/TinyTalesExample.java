package com.tinytales.example;

import com.tinytales.example.models.Author;
import com.tinytales.example.models.Story;
import com.tinytales.example.services.TinyTalesService;
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
 * Main application for Tiny Tales - Short stories for children
 */
public class TinyTalesExample {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_URL = "http://localhost:1080";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Admin13579++";
    private static final String SITE_NAME = "Tiny Tales - Stories for Children";

    public static void main(String[] args) {
        System.out.println("""
                ╔════════════════════════════════════════════════════════════╗
                ║     Tiny Tales - Short Stories for Children               ║
                ╚════════════════════════════════════════════════════════════╝
                """);

        ReactiveNodifyClient client = ReactiveNodifyClient.create(
                ReactiveNodifyClient.builder()
                        .withBaseUrl(BASE_URL)
                        .withTimeout(30000)
                        .build()
        );

        CountDownLatch latch = new CountDownLatch(1);

        runTinyTalesExample(client)
                .doFinally(signal -> {
                    if (client instanceof Disposable) {
                        ((Disposable) client).dispose();
                    }
                    latch.countDown();
                })
                .subscribe(
                        result -> System.out.println("\n✅ Tiny Tales example completed!"),
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

    private static Mono<Void> runTinyTalesExample(ReactiveNodifyClient client) {
        AtomicReference<String> siteCode = new AtomicReference<>();
        AtomicReference<String> storiesNodeCode = new AtomicReference<>();
        AtomicReference<String> styleCode = new AtomicReference<>();
        AtomicReference<String> scriptCode = new AtomicReference<>();
        AtomicReference<String> story1Code = new AtomicReference<>();
        AtomicReference<String> story2Code = new AtomicReference<>();
        AtomicReference<String> mainContentCode = new AtomicReference<>();

        return client.login(USERNAME, PASSWORD)
                .flatMap(auth -> {
                    System.out.println("✅ Authenticated successfully");
                    return createSite(client);
                })
                .flatMap(site -> {
                    siteCode.set(site.getCode());
                    System.out.println("📌 Site ready: " + site.getName());

                    // Créer le sous-nœud pour les histoires
                    return createStoriesNode(client, siteCode.get())
                            .flatMap(storiesNode -> {
                                storiesNodeCode.set(storiesNode.getCode());
                                System.out.println("📁 Stories node created: " + storiesNodeCode.get());

                                TinyTalesService service = new TinyTalesService(client, siteCode.get());

                                // Create content nodes
                                return createStyleContent(service, styleCode)
                                        .then(createStory1(service, story1Code, storiesNodeCode.get()))
                                        .then(createStory2(service, story2Code, storiesNodeCode.get()))
                                        // Create dynamic script with site code
                                        .then(Mono.defer(() ->
                                                createScriptContent(service, scriptCode, siteCode.get(), storiesNodeCode.get())))
                                        // Create main content with style and script codes
                                        .then(Mono.defer(() ->
                                                createMainContent(service,
                                                        styleCode.get(),
                                                        scriptCode.get(),
                                                        mainContentCode)))
                                        // Add translations for stories
                                        .then(Mono.defer(() -> addStoryTranslations(service,
                                                story1Code.get(),
                                                story2Code.get(),
                                                mainContentCode.get())))
                                        // Publish everything (nodes and content)
                                        .then(Mono.defer(() -> publishAll(service,
                                                siteCode.get(),
                                                storiesNodeCode.get(),
                                                styleCode.get(),
                                                scriptCode.get(),
                                                story1Code.get(),
                                                story2Code.get(),
                                                mainContentCode.get())))
                                        // Display final information
                                        .then(Mono.defer(() -> displayResults(service,
                                                siteCode.get(),
                                                storiesNodeCode.get(),
                                                styleCode.get(),
                                                scriptCode.get(),
                                                story1Code.get(),
                                                story2Code.get(),
                                                mainContentCode.get())));
                            });
                });
    }

    private static Mono<Node> createSite(ReactiveNodifyClient client) {
        Node site = new Node();
        site.setName(SITE_NAME);
        site.setCode("TINY-TALES-SITE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        site.setDescription("tiny-tales");
        site.setType("SITE");
        site.setStatus(StatusEnum.SNAPSHOT);
        site.setDefaultLanguage("en");
        return client.saveNode(site);
    }

    private static Mono<Void> createStyleContent(TinyTalesService service, AtomicReference<String> codeRef) {
        System.out.println("\n🎨 Creating style content...");

        String css = """
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                body {
                    font-family: 'Comic Sans MS', 'Chalkboard SE', cursive, sans-serif;
                    background: linear-gradient(135deg, #f9d5e5 0%, #d5f0f9 100%);
                    min-height: 100vh;
                    padding: 30px 20px;
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
                    border-radius: 60px 60px 30px 30px;
                    box-shadow: 0 15px 30px rgba(0,0,0,0.15);
                    border: 3px solid #ffb6c1;
                    position: relative;
                    overflow: hidden;
                }
                
                .header::before {
                    content: "✨";
                    font-size: 40px;
                    position: absolute;
                    left: 20px;
                    top: 20px;
                    opacity: 0.3;
                }
                
                .header::after {
                    content: "📚";
                    font-size: 40px;
                    position: absolute;
                    right: 20px;
                    bottom: 20px;
                    opacity: 0.3;
                }
                
                .header h1 {
                    color: #ff6b6b;
                    font-size: 3em;
                    margin-bottom: 15px;
                    text-shadow: 3px 3px 0 #ffeaa7;
                }
                
                .header p {
                    color: #5f9ea0;
                    font-size: 1.3em;
                    margin-bottom: 20px;
                }
                
                .language-selector {
                    margin: 20px auto;
                    padding: 15px;
                    background: #fff0f5;
                    border-radius: 50px;
                    display: inline-block;
                    border: 2px dashed #ffb6c1;
                }
                
                .language-selector select {
                    padding: 12px 25px;
                    border-radius: 30px;
                    border: 2px solid #ff9a9e;
                    background: white;
                    font-size: 1.1em;
                    font-family: 'Comic Sans MS', cursive;
                    cursor: pointer;
                }
                
                .stories-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 30px;
                    margin-top: 40px;
                }
                
                .story-card {
                    background: white;
                    border-radius: 40px 20px 40px 20px;
                    padding: 30px;
                    box-shadow: 0 15px 25px rgba(0,0,0,0.1);
                    transition: transform 0.3s ease;
                    border: 3px solid #98ddca;
                    position: relative;
                }
                
                .story-card:hover {
                    transform: translateY(-10px);
                }
                
                .story-card h2 {
                    color: #ff8a8a;
                    font-size: 2em;
                    margin-bottom: 15px;
                    border-bottom: 3px dotted #a7e9af;
                    padding-bottom: 10px;
                }
                
                .story-card p {
                    color: #5d6d7e;
                    font-size: 1.2em;
                    line-height: 1.6;
                    margin-bottom: 15px;
                }
                
                .story-card .animal-icon {
                    font-size: 50px;
                    text-align: right;
                    margin-top: 10px;
                }
                
                .footer {
                    text-align: center;
                    margin-top: 60px;
                    padding: 30px;
                    background: white;
                    border-radius: 30px 30px 60px 60px;
                    border: 3px solid #ffb6c1;
                    color: #5f9ea0;
                    font-size: 1.2em;
                }
                """;

        Author author = new Author("Tiny Tales Team", "hello@tinytales.com",
                "Creating magical stories for children", null);

        Story styleStory = new Story(
                null,
                "Tiny Tales Styles",
                css,
                "CSS styles for Tiny Tales",
                author,
                List.of("css", "styles"),
                "tiny-tales-styles",
                "en",
                ContentTypeEnum.STYLE,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createStory(styleStory)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created style content with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createScriptContent(TinyTalesService service,
                                                  AtomicReference<String> codeRef,
                                                  String siteCode,
                                                  String storiesNodeCode) {
        System.out.println("\n📜 Creating dynamic script content...");

        String script = String.format("""
            const SITE_CODE = '%s';
            const STORIES_NODE_CODE = '%s';
            const API_URL = 'https://nodify-api.azirar.ovh';
            
            function changeLanguage(lang) {
                const url = new URL(window.location.href);
                url.searchParams.set('translation', lang);
                window.location.href = url.toString();
            }
            
            async function fetchAllStories() {
                const url = `${API_URL}/contents/node/code/${STORIES_NODE_CODE}?status=SNAPSHOT`;
                try {
                    const response = await fetch(url);
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    return await response.json();
                } catch (error) {
                    console.error('Error fetching stories:', error);
                    return [];
                }
            }
            
            async function loadStoryContent(code, lang = 'en') {
                const url = `${API_URL}/contents/code/${code}?payloadOnly=true&status=SNAPSHOT&translation=${lang}`;
                try {
                    const response = await fetch(url);
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    const data = await response.json();
                    
                    // Si c'est un objet JSON, le retourner directement
                    // Si c'est une chaîne JSON, la parser
                    if (typeof data === 'object' && data !== null) {
                        return data;
                    }
                    return typeof data === 'string' ? JSON.parse(data) : data;
                } catch (error) {
                    console.error('Error fetching story:', error);
                    return null;
                }
            }
            
            async function loadStories() {
                const storiesGrid = document.querySelector('.stories-grid');
                if (!storiesGrid) return;
                
                storiesGrid.innerHTML = '';
                
                const params = new URLSearchParams(window.location.search);
                const lang = params.get('translation') || 'en';
                
                // Récupérer tous les contenus du sous-nœud STORIES
                const allContents = await fetchAllStories();
                console.log('All contents from stories node:', allContents);
                
                // Filtrer uniquement les contenus de type JSON (les histoires)
                const stories = allContents.filter(content => content.type === 'JSON');
                console.log('Filtered JSON stories:', stories);
                
                const animalIcons = ['🐶', '🐱', '🦊', '🐼', '🐨', '🦁', '🐸', '🐧'];
                
                for (let i = 0; i < stories.length; i++) {
                    const story = stories[i];
                    const storyData = await loadStoryContent(story.code, lang);
                    console.log('Story data for', story.code, ':', storyData);
                    
                    if (!storyData) continue;
                    
                    const card = document.createElement('div');
                    card.className = 'story-card';
                    
                    const title = document.createElement('h2');
                    title.textContent = storyData.title || 'A Tiny Tale';
                    card.appendChild(title);
                    
                    if (storyData.content) {
                        const content = document.createElement('p');
                        content.textContent = storyData.content;
                        card.appendChild(content);
                    }
                    
                    const icon = document.createElement('div');
                    icon.className = 'animal-icon';
                    icon.textContent = animalIcons[i %% animalIcons.length];
                    card.appendChild(icon);
                    
                    storiesGrid.appendChild(card);
                }
                
                if (stories.length === 0) {
                    storiesGrid.innerHTML = '<p style="text-align:center; font-size:1.5em;">✨ No stories yet, but check back soon! ✨</p>';
                }
            }
            
            window.addEventListener('load', function() {
                const params = new URLSearchParams(window.location.search);
                const lang = params.get('translation') || 'en';
                const select = document.querySelector('select');
                if (select) select.value = lang;
                loadStories();
            });
            """, siteCode, storiesNodeCode);

        Author author = new Author("Tiny Tales Team", "hello@tinytales.com",
                "Creating magical stories for children", null);

        Story scriptStory = new Story(
                null,
                "Tiny Tales Script",
                script,
                "JavaScript for Tiny Tales",
                author,
                List.of("js", "javascript", "dynamic"),
                "tiny-tales-script",
                "en",
                ContentTypeEnum.SCRIPT,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createStory(scriptStory)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created dynamic script content with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createStory1(TinyTalesService service,
                                           AtomicReference<String> codeRef,
                                           String storiesNodeCode) {
        System.out.println("\n📖 Creating story 1: The Little Fox...");

        String jsonContent = """
            {
              "title": "$translate(TITLE)",
              "content": "$translate(CONTENT)"
            }
            """;

        Author author = new Author("Emma Thompson", "emma@tinytales.com",
                "Children's book author", null);

        Story story = new Story(
                null,
                "The Little Fox Who Lost His Spots",
                jsonContent,
                "Story 1",
                author,
                List.of("fox", "forest", "friendship"),
                "little-fox",
                "en",
                ContentTypeEnum.JSON,
                Instant.now(),
                Instant.now(),
                false
        );

        // Utiliser le sous-nœud comme parent au lieu du site principal
        return service.createStory(story, storiesNodeCode)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created story 1 with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> createStory2(TinyTalesService service,
                                           AtomicReference<String> codeRef,
                                           String storiesNodeCode) {
        System.out.println("\n📖 Creating story 2: The Sleepy Bear...");

        String jsonContent = """
            {
              "title": "$translate(TITLE)",
              "content": "$translate(CONTENT)"
            }
            """;

        Author author = new Author("Michael Brown", "michael@tinytales.com",
                "Storyteller and illustrator", null);

        Story story = new Story(
                null,
                "The Sleepy Bear and the Honey Moon",
                jsonContent,
                "Story 2",
                author,
                List.of("bear", "honey", "moon"),
                "sleepy-bear",
                "en",
                ContentTypeEnum.JSON,
                Instant.now(),
                Instant.now(),
                false
        );

        // Utiliser le sous-nœud comme parent
        return service.createStory(story, storiesNodeCode)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created story 2 with code: " + codeRef.get());
                })
                .then();
    }


    private static Mono<Void> createMainContent(TinyTalesService service,
                                                String styleCode,
                                                String scriptCode,
                                                AtomicReference<String> codeRef) {
        System.out.println("\n📄 Creating main Tiny Tales page...");

        String mainHtml = String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Tiny Tales - Stories for Children</title>
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
                                </select>
                            </div>
                            <p><small>✨ $translate(CURRENT_LANG) ✨</small></p>
                        </div>
                
                        <div class="stories-grid">
                          <!-- Stories will be loaded dynamically -->
                        </div>
                
                        <div class="footer">
                            <p>$translate(FOOTER_TEXT)</p>
                            <p>💫 $translate(MORE_COMING) 💫</p>
                        </div>
                    </div>
                
                    <script>
                        $content(%s)
                    </script>
                </body>
                </html>
                """, styleCode, scriptCode);

        Author author = new Author("Tiny Tales Team", "hello@tinytales.com",
                "Creating magical stories for children", null);

        Story mainStory = new Story(
                null,
                "Tiny Tales Main Page",
                mainHtml,
                "Main page for Tiny Tales",
                author,
                List.of("main", "stories", "children"),
                "tiny-tales",
                "en",
                ContentTypeEnum.HTML,
                Instant.now(),
                Instant.now(),
                false
        );

        return service.createStory(mainStory)
                .doOnNext(saved -> {
                    codeRef.set(saved.id());
                    System.out.println("✅ Created main page with code: " + codeRef.get());
                })
                .then();
    }

    private static Mono<Void> addStoryTranslations(TinyTalesService service,
                                                   String story1Code,
                                                   String story2Code,
                                                   String mainPageCode) {  // ← Ajout du paramètre
        System.out.println("\n🌍 Adding translations for stories...");

        // Story 1 translations
        return service.addTranslationKey(story1Code, "en", "TITLE", "The Little Fox Who Lost His Spots")
                .then(service.addTranslationKey(story1Code, "en", "CONTENT",
                        "Once upon a time, a little fox woke up and found that all his spots had disappeared overnight. He searched everywhere - under the leaves, behind the trees, and even asked his friends the rabbits. Finally, he looked in the pond and saw his reflection - his spots were there all along! They had just faded in the morning light. From that day on, he never worried about losing his spots again."))

                .then(service.addTranslationKey(story1Code, "fr", "TITLE", "Le Petit Renard qui a Perdu ses Taches"))
                .then(service.addTranslationKey(story1Code, "fr", "CONTENT",
                        "Il était une fois un petit renard qui se réveilla et découvrit que toutes ses taches avaient disparu pendant la nuit. Il chercha partout - sous les feuilles, derrière les arbres, et demanda même à ses amis les lapins. Finalement, il regarda dans l'étang et vit son reflet - ses taches étaient là depuis toujours ! Elles s'étaient juste estompées dans la lumière du matin. À partir de ce jour, il ne s'inquiéta plus jamais de perdre ses taches."))

                // Story 2 translations
                .then(service.addTranslationKey(story2Code, "en", "TITLE", "The Sleepy Bear and the Honey Moon"))
                .then(service.addTranslationKey(story2Code, "en", "CONTENT",
                        "Bear was supposed to hibernate, but he just couldn't fall asleep. The moon was so bright and round, looking just like a jar of honey. So Bear climbed the tallest tree to get a closer look. He reached and reached, but the moon was always just out of reach. Tired from his adventure, he climbed back down and fell fast asleep, dreaming of honey moons."))

                .then(service.addTranslationKey(story2Code, "fr", "TITLE", "L'Ours Endormi et la Lune de Miel"))
                .then(service.addTranslationKey(story2Code, "fr", "CONTENT",
                        "L'ours était censé hiberner, mais il n'arrivait pas à s'endormir. La lune était si brillante et ronde, ressemblant à un pot de miel. Alors l'ours grimpa à l'arbre le plus haut pour mieux voir. Il tendit la patte, encore et encore, mais la lune était toujours hors de portée. Fatigué de son aventure, il redescendit et s'endormit profondément, rêvant de lunes de miel."))

                // Main page translations
                .then(service.addTranslationKey(mainPageCode, "en", "MAIN_TITLE", "Tiny Tales"))
                .then(service.addTranslationKey(mainPageCode, "en", "MAIN_DESCRIPTION", "Short, magical stories for children"))
                .then(service.addTranslationKey(mainPageCode, "en", "CURRENT_LANG", "English"))
                .then(service.addTranslationKey(mainPageCode, "en", "FOOTER_TEXT", "Made with 💕 for little dreamers"))
                .then(service.addTranslationKey(mainPageCode, "en", "MORE_COMING", "More stories coming soon!"))

                .then(service.addTranslationKey(mainPageCode, "fr", "MAIN_TITLE", "Petites Histoires"))
                .then(service.addTranslationKey(mainPageCode, "fr", "MAIN_DESCRIPTION", "De courtes histoires magiques pour enfants"))
                .then(service.addTranslationKey(mainPageCode, "fr", "CURRENT_LANG", "Français"))
                .then(service.addTranslationKey(mainPageCode, "fr", "FOOTER_TEXT", "Fabriqué avec 💕 pour les petits rêveurs"))
                .then(service.addTranslationKey(mainPageCode, "fr", "MORE_COMING", "Bientôt d'autres histoires !"))
                .then();
    }

    private static Mono<Void> publishAll(TinyTalesService service,
                                         String siteCode,
                                         String storiesNodeCode,
                                         String styleCode,
                                         String scriptCode,
                                         String story1Code,
                                         String story2Code,
                                         String mainCode) {
        System.out.println("\n🚀 Publishing all nodes and content...");

        // Publier les nœuds d'abord
        return service.publishNode(siteCode)
                .doOnNext(node -> System.out.println("  ✅ Published node: " + node.getName()))
                .then(service.publishNode(storiesNodeCode))
                .doOnNext(node -> System.out.println("  ✅ Published node: " + node.getName()))
                // Ensuite publier les contenus
                .then(service.publishStory(styleCode))
                .doOnNext(story -> System.out.println("  ✅ Published story: " + story.title()))
                .then(service.publishStory(scriptCode))
                .doOnNext(story -> System.out.println("  ✅ Published story: " + story.title()))
                .then(service.publishStory(story1Code))
                .doOnNext(story -> System.out.println("  ✅ Published story: " + story.title()))
                .then(service.publishStory(story2Code))
                .doOnNext(story -> System.out.println("  ✅ Published story: " + story.title()))
                .then(service.publishStory(mainCode))
                .doOnNext(story -> System.out.println("  ✅ Published story: " + story.title()))
                .then();
    }

    private static Mono<Void> displayResults(TinyTalesService service,
                                             String siteCode,
                                             String storiesNodeCode,
                                             String styleCode,
                                             String scriptCode,
                                             String story1Code,
                                             String story2Code,
                                             String mainCode) {
        System.out.println("\n📊 Final Results:");
        System.out.println("  📌 Site node code: " + siteCode);
        System.out.println("  📌 Stories node code: " + storiesNodeCode);
        System.out.println("  📌 Style content code: " + styleCode);
        System.out.println("  📌 Script content code: " + scriptCode);
        System.out.println("  📌 Story 1 code: " + story1Code);
        System.out.println("  📌 Story 2 code: " + story2Code);
        System.out.println("  📌 Main page code: " + mainCode);

        System.out.println("\n🔍 Access URLs:");
        System.out.println("  Main page: " + BASE_URL + "/v0/content-node/code/" + mainCode);
        System.out.println("  Story 1 (EN): " + API_URL + "/contents/code/" + story1Code + "?payloadOnly=true&status=PUBLISHED&translation=en");
        System.out.println("  Story 1 (FR): " + API_URL + "/contents/code/" + story1Code + "?payloadOnly=true&status=PUBLISHED&translation=fr");
        System.out.println("  Story 2 (EN): " + API_URL + "/contents/code/" + story2Code + "?payloadOnly=true&status=PUBLISHED&translation=en");
        System.out.println("  Story 2 (FR): " + API_URL + "/contents/code/" + story2Code + "?payloadOnly=true&status=PUBLISHED&translation=fr");

        return Mono.empty();
    }

    private static Mono<Node> createStoriesNode(ReactiveNodifyClient client, String parentCode) {
        Node storiesNode = new Node();
        storiesNode.setParentCode(parentCode);
        storiesNode.setName("Stories");
        storiesNode.setCode("STORIES-NODE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        storiesNode.setDescription("stories");
        storiesNode.setType("FOLDER");
        storiesNode.setStatus(StatusEnum.SNAPSHOT);
        storiesNode.setDefaultLanguage("en");
        return client.saveNode(storiesNode);
    }
}