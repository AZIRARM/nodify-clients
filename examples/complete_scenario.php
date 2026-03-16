<?php

require_once __DIR__ . '/../../vendor/autoload.php';

use Nodify\Client;
use Nodify\Node;
use Nodify\ContentNode;
use Nodify\Translation;
use Nodify\Value;
use Nodify\ContentTypeEnum;
use Nodify\StatusEnum;
use Nodify\Exception\NodifyClientException;

function main() {
    $client = Client::create(
        Client::builder()
            ->withBaseUrl('https://nodify-core.azirar.ovh')
            ->withTimeout(30000)
            ->build()
    );

    try {
        $auth = $client->login('admin', 'Admin13579++');
        echo "✅ Authenticated successfully\n";

        createCompleteScenario($client);

        echo "✅ Scenario completed successfully!\n";

    } catch (NodifyClientException $e) {
        echo "❌ Error: {$e->getMessage()}\n";
    } catch (Exception $e) {
        echo "❌ Unexpected error: {$e->getMessage()}\n";
    }
}

function createCompleteScenario($client) {
    echo "\n" . str_repeat("=", 60) . "\n";
    echo "DÉBUT DU SCÉNARIO COMPLET\n";
    echo str_repeat("=", 60) . "\n";

    // Step 1: Create parent node
    echo "\n📌 Step 1: Creating parent node...\n";
    $parentNode = createParentNode($client);
    echo "   ✅ Parent node: {$parentNode->name} (Code: {$parentNode->code})\n";

    // Step 2: Create child node
    echo "\n📌 Step 2: Creating child node...\n";
    $childNode = createChildNode($client, $parentNode->code);
    echo "   ✅ Child node: {$childNode->name} (Code: {$childNode->code})\n";
    echo "   Child parent_code: {$childNode->parentCode}\n";

    // Step 3: Create HTML content
    echo "\n📌 Step 3: Creating HTML content...\n";
    $contentNode = createHtmlContent($client, $childNode->code);
    echo "   ✅ HTML content created with code: {$contentNode->code}\n";
    echo "   Content parent_code: {$contentNode->parentCode}\n";

    // Step 4: Add translations
    echo "\n📌 Step 4: Adding translations...\n";
    $contentNode = createTranslations($client, $contentNode);
    echo "   ✅ Translations added\n";

    // Step 5: Add user name value
    echo "\n📌 Step 5: Adding user name value...\n";
    $contentNode = createUserNameValue($client, $contentNode);
    echo "   ✅ User name value added\n";

    // Step 6: Publish content
    echo "\n📌 Step 6: Publishing content...\n";
    publishContent($client, $contentNode->code);
    echo "   ✅ Content published\n";

    // Step 7: Publish parent node
    echo "\n📌 Step 7: Publishing parent node...\n";
    publishNode($client, $parentNode->code);
    echo "   ✅ Parent node published\n";

    displayFinalInfo($parentNode, $childNode, $contentNode);
}

function createParentNode($client) {
    $node = new Node();
    $node->name = "My English Website written with PHP";
    $node->code = "SITE-EN-" . strtoupper(substr(uniqid(), -8));
    $node->slug = "my-english-website";
    $node->environmentCode = "production";
    $node->defaultLanguage = "EN";
    $node->description = "My personal website with English content";
    $node->type = "SITE";
    $node->status = StatusEnum::SNAPSHOT;

    return $client->saveNode($node);
}

function createChildNode($client, $parentCode) {
    $node = new Node();
    $node->parentCode = $parentCode;
    $node->name = "Welcome Page";
    $node->code = "PAGE-WELCOME-" . strtoupper(substr(uniqid(), -8));
    $node->slug = "welcome";
    $node->environmentCode = "production";
    $node->defaultLanguage = "EN";
    $node->description = "Welcome page with dynamic content";
    $node->type = "PAGE";
    $node->status = StatusEnum::SNAPSHOT;

    return $client->saveNode($node);
}

function createHtmlContent($client, $nodeCode) {
    $content = new ContentNode();
    $content->parentCode = $nodeCode;
    $content->code = "HTML-" . strtoupper(substr(uniqid(), -8));
    $content->slug = "welcome-message";
    $content->environmentCode = "production";
    $content->language = "EN";
    $content->type = ContentTypeEnum::HTML;
    $content->title = "Welcome Message with Dynamic Content";
    $content->description = "Dynamic welcome page using translations and values";
    $content->status = StatusEnum::SNAPSHOT;
    $content->content = getHtmlTemplate();

    return $client->saveContentNode($content);
}

function createTranslations($client, $contentNode) {
    $translations = [
        // HELLO_WORLD translations
        new Translation("HELLO_WORLD", "EN", "Hello World"),
        new Translation("HELLO_WORLD", "FR", "Bonjour le monde"),
        new Translation("HELLO_WORLD", "ES", "¡Hola Mundo"),
        new Translation("HELLO_WORLD", "DE", "Hallo Welt"),
        new Translation("HELLO_WORLD", "IT", "Ciao Mondo"),
        new Translation("HELLO_WORLD", "PT", "Olá Mundo"),
        new Translation("HELLO_WORLD", "NL", "Hallo Wereld"),
        new Translation("HELLO_WORLD", "RU", "Привет мир"),
        new Translation("HELLO_WORLD", "JA", "こんにちは世界"),
        new Translation("HELLO_WORLD", "ZH", "你好世界"),

        // EXPLORE_MORE translations
        new Translation("EXPLORE_MORE", "EN", "Explore more"),
        new Translation("EXPLORE_MORE", "FR", "Explorer plus"),
        new Translation("EXPLORE_MORE", "ES", "Explorar más"),
        new Translation("EXPLORE_MORE", "DE", "Mehr entdecken"),
        new Translation("EXPLORE_MORE", "IT", "Esplora di più"),
        new Translation("EXPLORE_MORE", "PT", "Explorar mais"),
        new Translation("EXPLORE_MORE", "NL", "Meer ontdekken"),
        new Translation("EXPLORE_MORE", "RU", "Узнать больше"),
        new Translation("EXPLORE_MORE", "JA", "さらに探索"),
        new Translation("EXPLORE_MORE", "ZH", "探索更多")
    ];

    $contentNode->translations = $translations;
    return $client->saveContentNode($contentNode);
}

function createUserNameValue($client, $contentNode) {
    $value = new Value();
    $value->key = "USER_NAME";
    $value->value = "John Doe";

    $contentNode->values = [$value];
    return $client->saveContentNode($contentNode);
}

function publishContent($client, $contentCode) {
    return $client->publishContentNode($contentCode, true);
}

function publishNode($client, $nodeCode) {
    return $client->publishNode($nodeCode);
}

function displayFinalInfo($parentNode, $childNode, $contentNode) {
    echo "\n" . str_repeat("=", 60) . "\n";
    echo "🎯 FINAL SCENARIO SUMMARY\n";
    echo str_repeat("=", 60) . "\n";

    echo "\n📁 PARENT NODE:\n";
    echo "   - Name: {$parentNode->name}\n";
    echo "   - Code: {$parentNode->code}\n";
    echo "   - Parent: " . ($parentNode->parentCode ?? 'None (root)') . "\n";

    echo "\n📄 CHILD NODE:\n";
    echo "   - Name: {$childNode->name}\n";
    echo "   - Code: {$childNode->code}\n";
    echo "   - Parent: {$childNode->parentCode}\n";

    echo "\n📝 CONTENT NODE:\n";
    echo "   - Code: {$contentNode->code}\n";
    echo "   - Title: {$contentNode->title}\n";
    echo "   - Parent: {$contentNode->parentCode}\n";

    echo "\n🌍 TRANSLATIONS:\n";
    if ($contentNode->translations) {
        foreach ($contentNode->translations as $t) {
            if (is_array($t)) {
                echo "   - {$t['key']} [{$t['language']}]: {$t['value']}\n";
            } else {
                echo "   - {$t->key} [{$t->language}]: {$t->value}\n";
            }
        }
    } else {
        echo "   ❌ No translations found!\n";
    }

    echo "\n🔢 VALUES:\n";
    if ($contentNode->values) {
        foreach ($contentNode->values as $v) {
            if (is_array($v)) {
                echo "   - {$v['key']}: {$v['value']}\n";
            } else {
                echo "   - {$v->key}: {$v->value}\n";
            }
        }
    } else {
        echo "   ❌ No values found!\n";
    }

    echo "\n📊 HIERARCHY:\n";
    echo "   {$parentNode->name} ({$parentNode->code})\n";
    echo "   └── {$childNode->name} ({$childNode->code})\n";
    echo "       └── Content: {$contentNode->title} ({$contentNode->code})\n";

    echo "\n" . str_repeat("=", 60) . "\n";
}

function getHtmlTemplate() {
    return <<<'HTML'
'<!DOCTYPE html>
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
             font-family: \'Segoe UI\', Tahoma, Geneva, Verdana, sans-serif;
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
             <!-- Using $value directive for dynamic user name -->
             <p>Welcome <strong>$value(USER_NAME)</strong>! We\'re glad to have you here.</p>
             <p>This message is automatically translated based on your language preference.</p>
         </div>

         <div class="user-greeting">
             <p>✨ <strong>$value(USER_NAME)</strong>\'s personalized greeting in different languages:</p>
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
             <p>Created with ❤️ using Nodify PHP Client</p>
             <p>© 2024 - Dynamic Content Powered by $translate(HELLO_WORLD)</p>
             <p style="font-size: 0.8em; margin-top: 10px;">
                 Current user: <strong>$value(USER_NAME)</strong>
             </p>
         </div>
     </div>

     <script>
         // Simple language switcher demonstration
         document.querySelector(\'select\').addEventListener(\'change\', function(e) {
                const url = new URL(window.location.href);
                url.searchParams.set(\'translation\', e.target.value);
                window.location.href = url.toString();
            });
     </script>
 </body>
 </html>'
HTML;
}

main();