// exemple-utilisation.ts

import { ContentNode, ContentNodeType, Node, NodeStatus, Translation, Value } from "../models/nodify-models";
import { NodifyClient } from "../nodify-client";

async function createCompleteScenario() {
  try {
    // Create reactive client (similar to Java)
    const client = NodifyClient.builder()
      .withBaseUrl('https://nodify-core.azirar.ovh')
      .withTimeout(30000)
      .build();

    // Login
    const auth = await client.login('admin', 'Admin13579++');
    console.log('✅ Authenticated successfully');

    // Step 1: Create parent node with EN as default language
    const parentNode: Node = {
      name: 'My English Website written with Node JS',
      code: 'SITE-EN-' + Math.random().toString(36).substring(2, 10).toUpperCase(),
      slug: 'my-english-website',
      environmentCode: 'production',
      defaultLanguage: 'EN',
      description: 'My personal website with English content',
      type: 'SITE',
      status: NodeStatus.SNAPSHOT
    };

    const savedParent = await client.saveNode(parentNode);
    console.log('✅ Parent node created:', savedParent.name, '(Code:', savedParent.code + ')');

    // Step 2: Create a child node under the parent
    const childNode: Node = {
      parentCode: savedParent.code,
      name: 'Welcome Page',
      code: 'PAGE-WELCOME-' + Math.random().toString(36).substring(2, 10).toUpperCase(),
      slug: 'welcome',
      environmentCode: 'production',
      defaultLanguage: 'EN',
      description: 'Welcome page with dynamic content',
      type: 'PAGE',
      status: NodeStatus.SNAPSHOT
    };

    const savedChild = await client.saveNode(childNode);
    console.log('✅ Child node created:', savedChild.name, '(Code:', savedChild.code + ')');

    // Step 3: Create HTML content with $translate and $val directives
    const htmlContent: ContentNode = {
      parentCode: savedChild.code,
      code: 'HTML-' + Math.random().toString(36).substring(2, 10).toUpperCase(),
      slug: 'welcome-message',
      environmentCode: 'production',
      language: 'EN',
      type: ContentNodeType.HTML,
      title: 'Welcome Message with Dynamic Content',
      description: 'Dynamic welcome page using translations and values',
      status: NodeStatus.SNAPSHOT,
      translations: [],
      values: []
    };

    // Step 4: Create translations for HELLO_WORLD and EXPLORE_MORE
    htmlContent.translations = [
      // HELLO_WORLD translations
      { key: 'HELLO_WORLD', language: 'EN', value: 'Hello World' },
      { key: 'HELLO_WORLD', language: 'FR', value: 'Bonjour le monde' },
      { key: 'HELLO_WORLD', language: 'ES', value: '¡Hola Mundo' },
      { key: 'HELLO_WORLD', language: 'DE', value: 'Hallo Welt' },
      { key: 'HELLO_WORLD', language: 'IT', value: 'Ciao Mondo' },
      { key: 'HELLO_WORLD', language: 'PT', value: 'Olá Mundo' },
      { key: 'HELLO_WORLD', language: 'NL', value: 'Hallo Wereld' },
      { key: 'HELLO_WORLD', language: 'RU', value: 'Привет мир' },
      { key: 'HELLO_WORLD', language: 'JA', value: 'こんにちは世界' },
      { key: 'HELLO_WORLD', language: 'ZH', value: '你好世界' },

      // EXPLORE_MORE translations
      { key: 'EXPLORE_MORE', language: 'EN', value: 'Explore more' },
      { key: 'EXPLORE_MORE', language: 'FR', value: 'Explorer plus' },
      { key: 'EXPLORE_MORE', language: 'ES', value: 'Explorar más' },
      { key: 'EXPLORE_MORE', language: 'DE', value: 'Mehr entdecken' },
      { key: 'EXPLORE_MORE', language: 'IT', value: 'Esplora di più' },
      { key: 'EXPLORE_MORE', language: 'PT', value: 'Explorar mais' },
      { key: 'EXPLORE_MORE', language: 'NL', value: 'Meer ontdekken' },
      { key: 'EXPLORE_MORE', language: 'RU', value: 'Узнать больше' },
      { key: 'EXPLORE_MORE', language: 'JA', value: 'さらに探索' },
      { key: 'EXPLORE_MORE', language: 'ZH', value: '探索更多' }
    ];

    // Step 5: Create USER_NAME value
    htmlContent.values = [
      { key: 'USER_NAME', value: 'John Doe' }
    ];

    // HTML content with $translate and $val directives (same as Java)
    htmlContent.content = `
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
            <a href="#" class="highlight">$translate(EXPLORE_MORE)</a>
        </div>

        <div class="footer">
            <p>Created with ❤️ using Nodify Node Client</p>
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
</html>`;

    // Save the content node
    const savedContent = await client.saveContentNode(htmlContent);
    console.log('✅ HTML content created with translations and values');

    // Publish the content
    const publishedContent = await client.publishContentNode(savedContent.code!, true);
    console.log('✅ Content published');

    // Publish the parent node
    const publishedParent = await client.publishNode(savedParent.code!);
    console.log('✅ Parent node published');

    // Display final information
    displayFinalInfo(savedParent, savedChild, savedContent);

  } catch (error) {
    console.error('❌ Error:', error);
  }
}

function displayFinalInfo(parentNode: Node, childNode: Node, content: ContentNode) {
  console.log('\n' + '='.repeat(60));
  console.log('🎯 FINAL SCENARIO SUMMARY');
  console.log('='.repeat(60));

  console.log('\n📁 PARENT NODE:');
  console.log('   - Name:', parentNode.name);
  console.log('   - Code:', parentNode.code);
  console.log('   - Default Language:', parentNode.defaultLanguage);
  console.log('   - Slug:', parentNode.slug);

  console.log('\n📄 CHILD NODE:');
  console.log('   - Name:', childNode.name);
  console.log('   - Code:', childNode.code);
  console.log('   - Parent:', childNode.parentCode);
  console.log('   - Slug:', childNode.slug);

  console.log('\n🌍 TRANSLATIONS CREATED:');
  if (content.translations) {
    content.translations.forEach(t => {
      console.log(`   - ${t.key} [${t.language}]: ${t.value}`);
    });
  }

  console.log('\n📝 HTML CONTENT:');
  console.log('   - Code:', content.code);
  console.log('   - Title:', content.title);
  console.log('   - Directives used:');
  console.log('      • $translate(HELLO_WORLD) - Displays greeting in user\'s language');
  console.log('      • $value(USER_NAME) - Displays the user name');
  console.log('      • $translate(EXPLORE_MORE) - Translated button text');

  console.log('\n🔍 HOW TO ACCESS:');
  console.log(`   Parent Node: GET /v0/nodes/code/${parentNode.code}`);
  console.log(`   Child Node: GET /v0/nodes/code/${childNode.code}`);
  console.log(`   Content: GET /v0/content-node/code/${content.code}`);

  console.log('\n✨ The HTML content will automatically:');
  console.log('   - Translate \'HELLO_WORLD\' based on language selection');
  console.log('   - Insert the user name \'John Doe\' via $value(USER_NAME)');
  console.log('   - Show multilingual greetings with the user name');

  console.log('\n' + '='.repeat(60));
}

// Run the example
createCompleteScenario();