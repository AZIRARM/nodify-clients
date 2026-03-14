// NodifyReactiveExample.java
package com.example;

import com.itexpert.content.client.ReactiveNodifyClient;
import com.itexpert.content.lib.enums.ContentTypeEnum;
import com.itexpert.content.lib.enums.StatusEnum;
import com.itexpert.content.lib.models.ContentNode;
import com.itexpert.content.lib.models.Node;
import com.itexpert.content.lib.models.Translation;
import com.itexpert.content.lib.models.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NodifyReactiveExample {

    public static void main(String[] args) {
        // Create reactive client
        ReactiveNodifyClient client = ReactiveNodifyClient.create(
                ReactiveNodifyClient.builder()
                        .withBaseUrl("https://nodify-core.azirar.ovh")
                        .withTimeout(30000)
                        .build()
        );

        // Execute the scenario
        client.login("admin", "Admin13579++")
                .flatMap(auth -> {
                    System.out.println("✅ Authenticated successfully");
                    return createCompleteScenario(client);
                })
                .subscribe(
                        result -> System.out.println("✅ Scenario completed successfully!"),
                        error -> System.err.println("❌ Error: " + error.getMessage())
                );

        // Wait for reactive operations to complete
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Mono<Void> createCompleteScenario(ReactiveNodifyClient client) {
        // Step 1: Create parent node with EN as default language
        return createParentNode(client)
                .flatMap(parentNode -> {
                    System.out.println("✅ Parent node created: " + parentNode.getName() +
                            " (Code: " + parentNode.getCode() + ")");

                    // Step 2: Create a child node under the parent
                    return createChildNode(client, parentNode.getCode())
                            .flatMap(childNode -> {
                                System.out.println("✅ Child node created: " + childNode.getName() +
                                        " (Code: " + childNode.getCode() + ")");

                                // Step 3: Create HTML content with $translate and $val
                                return createHtmlContent(client, childNode.getCode())
                                        .flatMap(contentNode -> createTranslations(client, contentNode))  //4
                                        .flatMap(contentNode -> createUserNameValue(client, contentNode)) //5
                                        .flatMap(content -> {
                                            System.out.println("✅ HTML content created");

                                            // Step 6: Publish the content
                                            return publishContent(client, content.getCode())
                                                    .flatMap(published -> {
                                                        System.out.println("✅ Content published");

                                                        // Step 7: Publish the parent node
                                                        return publishNode(client, parentNode.getCode())
                                                                .map(publishedNode -> {
                                                                    System.out.println("✅ Parent node published");

                                                                    // Display final information
                                                                    displayFinalInfo(parentNode, childNode, content);

                                                                    return publishedNode;
                                                                });
                                                    });
                                        });
                            });
                })
                .then();
    }

    /**
     * Step 1: Create parent node with EN default language
     */
    private static Mono<Node> createParentNode(ReactiveNodifyClient client) {
        Node parentNode = new Node();
        parentNode.setName("My English Website written with Java");
        parentNode.setCode("SITE-EN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parentNode.setSlug("my-english-website");
        parentNode.setEnvironmentCode("production");
        parentNode.setDefaultLanguage("EN");
        parentNode.setDescription("My personal website with English content");
        parentNode.setType("SITE");
        parentNode.setStatus(StatusEnum.SNAPSHOT);

        return client.saveNode(parentNode);
    }

    /**
     * Step 2: Create a child node under the parent
     */
    private static Mono<Node> createChildNode(ReactiveNodifyClient client, String parentCode) {
        Node childNode = new Node();
        childNode.setParentCode(parentCode);
        childNode.setName("Welcome Page");
        childNode.setCode("PAGE-WELCOME-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        childNode.setSlug("welcome");
        childNode.setEnvironmentCode("production");
        childNode.setDefaultLanguage("EN");
        childNode.setDescription("Welcome page with dynamic content");
        childNode.setType("PAGE");
        childNode.setStatus(StatusEnum.SNAPSHOT);

        return client.saveNode(childNode);
    }

    /**
     * Step 3: Create translations for HELLO_WORLD
     * This creates a ContentNode of type DATA to store translations
     */
    private static Mono<ContentNode> createTranslations(ReactiveNodifyClient client, ContentNode contentNode) {
        // Create translations for HELLO_WORLD in multiple languages
        List<Translation> translations = Arrays.asList(
                createTranslation("HELLO_WORLD", "EN", "Hello World"),
                createTranslation("HELLO_WORLD", "FR", "Bonjour le monde"),
                createTranslation("HELLO_WORLD", "ES", "¡Hola Mundo"),
                createTranslation("HELLO_WORLD", "DE", "Hallo Welt"),
                createTranslation("HELLO_WORLD", "IT", "Ciao Mondo"),
                createTranslation("HELLO_WORLD", "PT", "Olá Mundo"),
                createTranslation("HELLO_WORLD", "NL", "Hallo Wereld"),
                createTranslation("HELLO_WORLD", "RU", "Привет мир"),
                createTranslation("HELLO_WORLD", "JA", "こんにちは世界"),
                createTranslation("HELLO_WORLD", "ZH", "你好世界"),

                createTranslation("EXPLORE_MORE", "EN", "Explore more"),
                createTranslation("EXPLORE_MORE", "FR", "Explorer plus"),
                createTranslation("EXPLORE_MORE", "ES", "Explorar más"),
                createTranslation("EXPLORE_MORE", "DE", "Mehr entdecken"),
                createTranslation("EXPLORE_MORE", "IT", "Esplora di più"),
                createTranslation("EXPLORE_MORE", "PT", "Explorar mais"),
                createTranslation("EXPLORE_MORE", "NL", "Meer ontdekken"),
                createTranslation("EXPLORE_MORE", "RU", "Узнать больше"),
                createTranslation("EXPLORE_MORE", "JA", "さらに探索"),
                createTranslation("EXPLORE_MORE", "ZH", "探索更多")

        );

        contentNode.setTranslations(translations);

        return client.saveContentNode(contentNode);
    }

    /**
     * Helper to create a translation object
     */
    private static Translation createTranslation(String key, String language, String value) {
        Translation translation = new Translation();
        translation.setKey(key);
        translation.setLanguage(language);
        translation.setValue(value);
        return translation;
    }

    /**
     * Step 4: Create USER_NAME value
     * This creates a Value object attached to the content
     */
    private static Mono<ContentNode> createUserNameValue(ReactiveNodifyClient client, ContentNode contentNode) {
        // Create a ContentNode to store values
        // Create USER_NAME value
        Value userNameValue = new Value();
        userNameValue.setKey("USER_NAME");
        userNameValue.setValue("John Doe");

        contentNode.setValues(Arrays.asList(userNameValue));

        return client.saveContentNode(contentNode)
                .map(c -> contentNode);
    }

    /**
     * Step 5: Create HTML content with $translate and $val directives
     */
    private static Mono<ContentNode> createHtmlContent(ReactiveNodifyClient client, String nodeCode) {
        ContentNode htmlContent = new ContentNode();
        htmlContent.setParentCode(nodeCode);
        htmlContent.setCode("HTML-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        htmlContent.setSlug("welcome-message");
        htmlContent.setEnvironmentCode("production");
        htmlContent.setLanguage("EN");
        htmlContent.setType(ContentTypeEnum.HTML);
        htmlContent.setTitle("Welcome Message with Dynamic Content");
        htmlContent.setDescription("Dynamic welcome page using translations and values");
        htmlContent.setStatus(StatusEnum.SNAPSHOT);

        // HTML content with $translate and $val directives
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome Page</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            margin: 0;
                            padding: 20px;
                        }
                
                        .container {
                            background: white;
                            border-radius: 20px;
                            padding: 50px;
                            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                            max-width: 800px;
                            width: 100%;
                            animation: slideIn 0.5s ease-out;
                        }
                
                        @keyframes slideIn {
                            from {
                                opacity: 0;
                                transform: translateY(-30px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }
                
                        h1 {
                            color: #333;
                            font-size: 3.5em;
                            margin-bottom: 30px;
                            text-align: center;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            -webkit-background-clip: text;
                            -webkit-text-fill-color: transparent;
                            background-clip: text;
                        }
                
                        .message {
                            font-size: 1.4em;
                            color: #666;
                            text-align: center;
                            margin-bottom: 40px;
                            line-height: 1.8;
                            padding: 30px;
                            background: #f5f5f5;
                            border-radius: 15px;
                            border-left: 5px solid #667eea;
                        }
                
                        .highlight {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            padding: 15px 30px;
                            border-radius: 50px;
                            display: inline-block;
                            font-weight: bold;
                            font-size: 1.2em;
                            text-decoration: none;
                            transition: transform 0.3s, box-shadow 0.3s;
                            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
                        }
                
                        .highlight:hover {
                            transform: scale(1.05);
                            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.6);
                        }
                
                        .user-greeting {
                            background: #e8f4fd;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 30px 0;
                            font-size: 1.2em;
                            color: #0066cc;
                            border: 2px dashed #667eea;
                        }
                
                        .footer {
                            margin-top: 40px;
                            text-align: center;
                            color: #999;
                            font-size: 1em;
                            border-top: 2px solid #eee;
                            padding-top: 25px;
                        }
                
                        .badge {
                            background: #4CAF50;
                            color: white;
                            padding: 8px 20px;
                            border-radius: 25px;
                            font-size: 0.9em;
                            display: inline-block;
                            margin-bottom: 25px;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                        }
                
                        .language-selector {
                            text-align: right;
                            margin-bottom: 20px;
                        }
                
                        .language-selector select {
                            padding: 8px 15px;
                            border-radius: 20px;
                            border: 1px solid #ddd;
                            font-size: 0.9em;
                            cursor: pointer;
                        }
                
                        .translation-demo {
                            display: flex;
                            justify-content: center;
                            gap: 15px;
                            flex-wrap: wrap;
                            margin: 25px 0;
                        }
                
                        .translation-demo span {
                            background: #f0f0f0;
                            padding: 5px 15px;
                            border-radius: 20px;
                            font-size: 0.9em;
                            color: #555;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="language-selector">
                            <select>
                                <option value="EN">English</option>
                                <option value="FR">Français</option>
                                <option value="ES">Español</option>
                                <option value="DE">Deutsch</option>
                                <option value="IT">Italiano</option>
                            </select>
                        </div>
                
                        <div style="text-align: center;">
                            <span class="badge">✨ Dynamic Content Demo ✨</span>
                        </div>
                
                        <!-- Using $translate directive for multilingual support -->
                        <h1>$translate(HELLO_WORLD)</h1>
                
                        <div class="message">
                            <!-- Using $val directive for dynamic user name -->
                            <p>Welcome <strong>$value(USER_NAME)</strong>! We're glad to have you here.</p>
                            <p>This message is automatically translated based on your language preference.</p>
                        </div>
                
                        <div class="user-greeting">
                            <p>✨ <strong>$value(USER_NAME)</strong>'s personalized greeting in different languages:</p>
                            <div class="translation-demo">
                                <span>🇫🇷 Bonjour $value(USER_NAME)</span>
                                <span>🇪🇸 ¡Hola $value(USER_NAME)</span>
                                <span>🇩🇪 Hallo $value(USER_NAME)</span>
                                <span>🇮🇹 Ciao $value(USER_NAME)</span>
                            </div>
                        </div>
                
                        <div style="text-align: center;">
                            <a href="https://github.com/AZIRARM/nodify" class="highlight">$translate(EXPLORE_MORE)</a>
                        </div>
                
                        <div class="footer">
                            <p>Created with ❤️ using Nodify Reactive Client</p>
                            <p>© 2024 - Dynamic Content Powered by $translate(HELLO_WORLD)</p>
                            <p style="font-size: 0.8em; margin-top: 10px;">
                                Current user: <strong>$value(USER_NAME)</strong>
                            </p>
                        </div>
                    </div>
                
                    <script>
                        // Simple language switcher demonstration
                        document.querySelector('select').addEventListener('change', function(e) {
                               const url = new URL(window.location.href);
                               url.searchParams.set('translation', e.target.value);
                               window.location.href = url.toString();
                           });
                    </script>
                </body>
                </html>
                """;

        htmlContent.setContent(html);

        return client.saveContentNode(htmlContent);
    }

    /**
     * Step 6: Publish the content
     */
    private static Mono<ContentNode> publishContent(ReactiveNodifyClient client, String contentCode) {
        return client.publishContentNode(contentCode, true);
    }

    /**
     * Step 7: Publish the parent node
     */
    private static Mono<Node> publishNode(ReactiveNodifyClient client, String nodeCode) {
        return client.publishNode(nodeCode);
    }

    /**
     * Display final information about created resources
     */
    private static void displayFinalInfo(Node parentNode, Node childNode,
                                         ContentNode content) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🎯 FINAL SCENARIO SUMMARY");
        System.out.println("=".repeat(60));

        System.out.println("\n📁 PARENT NODE:");
        System.out.println("   - Name: " + parentNode.getName());
        System.out.println("   - Code: " + parentNode.getCode());
        System.out.println("   - Default Language: " + parentNode.getDefaultLanguage());
        System.out.println("   - Slug: " + parentNode.getSlug());

        System.out.println("\n📄 CHILD NODE:");
        System.out.println("   - Name: " + childNode.getName());
        System.out.println("   - Code: " + childNode.getCode());
        System.out.println("   - Parent: " + childNode.getParentCode());
        System.out.println("   - Slug: " + childNode.getSlug());

        System.out.println("\n🌍 TRANSLATIONS CREATED:");
        if (content != null && content.getTranslations() != null) {
            content.getTranslations().forEach(t ->
                    System.out.println("   - " + t.getKey() + " [" + t.getLanguage() + "]: " + t.getValue())
            );
        }

        System.out.println("\n📝 HTML CONTENT:");
        System.out.println("   - Code: " + content.getCode());
        System.out.println("   - Title: " + content.getTitle());
        System.out.println("   - Directives used:");
        System.out.println("      • $translate(HELLO_WORLD) - Displays greeting in user's language");
        System.out.println("      • $value(USER_NAME) - Displays the user name");
        System.out.println("      • $translate(EXPLORE_MORE) - Translated button text");

        System.out.println("\n🔍 HOW TO ACCESS:");
        System.out.println("   Parent Node: GET /v0/nodes/code/" + parentNode.getCode());
        System.out.println("   Child Node: GET /v0/nodes/code/" + childNode.getCode());
        System.out.println("   Content: GET /v0/content-node/code/" + content.getCode());
        System.out.println("   Translations: GET /v0/content-node/code/" + content  .getCode());

        System.out.println("\n✨ The HTML content will automatically:");
        System.out.println("   - Translate 'HELLO_WORLD' based on language selection");
        System.out.println("   - Insert the user name 'John Doe' via $value(USER_NAME)");
        System.out.println("   - Show multilingual greetings with the user name");

        System.out.println("\n" + "=".repeat(60));
    }

    /**
     * Additional method to verify content with different languages
     */
    public static void testContentInDifferentLanguages(ReactiveNodifyClient client, String contentCode) {
        List<String> languages = Arrays.asList("EN", "FR", "ES", "DE", "IT");

        Flux.fromIterable(languages)
                .flatMap(lang -> {
                    System.out.println("\n🔍 Testing content in " + lang + "...");
                    return client.findContentNodeByCodeAndStatus(contentCode, "PUBLISHED")
                            .map(content -> {
                                System.out.println("   Content retrieved for language: " + lang);
                                return content;
                            });
                })
                .subscribe();
    }
}