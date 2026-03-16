"""
Nodify Python Client - Complete scenario example
Exact replica of NodifyReactiveExample.java
"""

import asyncio
import uuid
import sys
from typing import List, Optional

from nodify_client import (
    ReactiveNodifyClient,
    ReactiveNodifyClientConfig,
    NodifyClientException,
    Node,
    ContentNode,
    Translation,
    Value,
    ContentTypeEnum,
    StatusEnum,
    PaginationParams
)


async def main():
    """Main function"""

    # Create reactive client
    client = ReactiveNodifyClient.create(
        ReactiveNodifyClient.builder()
            .with_base_url("https://nodify-core.azirar.ovh")
            .with_timeout(30000)
            .build()
    )

    try:
        # Execute the scenario
        auth_response = await client.login("admin", "Admin13579++")
        print("✅ Authenticated successfully")

        # Run the complete scenario
        await create_complete_scenario(client)

        print("✅ Scenario completed successfully!")

    except NodifyClientException as e:
        print(f"❌ Error: {e}")
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
    finally:
        await client.close()

    # Wait a bit to see all output
    await asyncio.sleep(1)


async def create_complete_scenario(client: ReactiveNodifyClient) -> None:
    """
    Create complete scenario with parent node, child node, and HTML content
    """
    # Step 1: Create parent node with EN as default language
    parent_node = await create_parent_node(client)
    print(f"✅ Parent node created: {parent_node.name} (Code: {parent_node.code})")

    # Step 2: Create a child node under the parent
    child_node = await create_child_node(client, parent_node.code)
    print(f"✅ Child node created: {child_node.name} (Code: {child_node.code})")
    print(f"   Child parent_code: {child_node.parent_code}")  # Should be parent_node.code

    # Step 3: Create HTML content with $translate and $value
    content_node = await create_html_content(client, child_node.code)
    print(f"✅ HTML content created with code: {content_node.code}")
    print(f"   Content parent_code: {content_node.parent_code}")  # Should be child_node.code

    # Step 4: Add translations to the SAME content node
    content_node = await create_translations(client, content_node)
    print(f"✅ Translations added to content node")

    # Step 5: Add user name value to the SAME content node
    content_node = await create_user_name_value(client, content_node)
    print(f"✅ User name value added to content node")

    # Step 6: Publish the content
    published_content = await publish_content(client, content_node.code)
    print("✅ Content published")

    # Step 7: Publish the parent node
    published_parent = await publish_node(client, parent_node.code)
    print("✅ Parent node published")

    # Display final information
    display_final_info(parent_node, child_node, content_node)


async def create_parent_node(client: ReactiveNodifyClient) -> Node:
    """
    Step 1: Create parent node with EN default language
    """
    parent_node = Node(
        name="My English Website written with Python",
        code=f"SITE-EN-{str(uuid.uuid4())[:8].upper()}",
        slug="my-english-website",
        environment_code="production",
        default_language="EN",
        description="My personal website with English content",
        type="SITE",
        status=StatusEnum.SNAPSHOT
    )

    return await client.save_node(parent_node)


async def create_child_node(client: ReactiveNodifyClient, parent_code: str) -> Node:
    """
    Step 2: Create a child node under the parent
    """
    child_node = Node(
        parent_code=parent_code,
        name="Welcome Page",
        code=f"PAGE-WELCOME-{str(uuid.uuid4())[:8].upper()}",
        slug="welcome",
        environment_code="production",
        default_language="EN",
        description="Welcome page with dynamic content",
        type="PAGE",
        status=StatusEnum.SNAPSHOT
    )

    return await client.save_node(child_node)


async def create_html_content(client: ReactiveNodifyClient, node_code: str) -> ContentNode:
    """
    Step 3: Create HTML content with $translate and $value directives
    """
    html_content = ContentNode(
        parent_code=node_code,
        code=f"HTML-{str(uuid.uuid4())[:8].upper()}",
        slug="welcome-message",
        environment_code="production",
        language="EN",
        type=ContentTypeEnum.HTML,
        title="Welcome Message with Dynamic Content",
        description="Dynamic welcome page using translations and values",
        status=StatusEnum.SNAPSHOT,
        content=HTML_TEMPLATE
        # No translations or values yet - they will be added in subsequent steps
    )

    return await client.save_content_node(html_content)


async def create_translations(client: ReactiveNodifyClient, content_node: ContentNode) -> ContentNode:
    """
    Step 4: Create translations for HELLO_WORLD and EXPLORE_MORE
    """
    translations = [
        # HELLO_WORLD translations
        Translation(key="HELLO_WORLD", language="EN", value="Hello World"),
        Translation(key="HELLO_WORLD", language="FR", value="Bonjour le monde"),
        Translation(key="HELLO_WORLD", language="ES", value="¡Hola Mundo"),
        Translation(key="HELLO_WORLD", language="DE", value="Hallo Welt"),
        Translation(key="HELLO_WORLD", language="IT", value="Ciao Mondo"),
        Translation(key="HELLO_WORLD", language="PT", value="Olá Mundo"),
        Translation(key="HELLO_WORLD", language="NL", value="Hallo Wereld"),
        Translation(key="HELLO_WORLD", language="RU", value="Привет мир"),
        Translation(key="HELLO_WORLD", language="JA", value="こんにちは世界"),
        Translation(key="HELLO_WORLD", language="ZH", value="你好世界"),

        # EXPLORE_MORE translations
        Translation(key="EXPLORE_MORE", language="EN", value="Explore more"),
        Translation(key="EXPLORE_MORE", language="FR", value="Explorer plus"),
        Translation(key="EXPLORE_MORE", language="ES", value="Explorar más"),
        Translation(key="EXPLORE_MORE", language="DE", value="Mehr entdecken"),
        Translation(key="EXPLORE_MORE", language="IT", value="Esplora di più"),
        Translation(key="EXPLORE_MORE", language="PT", value="Explorar mais"),
        Translation(key="EXPLORE_MORE", language="NL", value="Meer ontdekken"),
        Translation(key="EXPLORE_MORE", language="RU", value="Узнать больше"),
        Translation(key="EXPLORE_MORE", language="JA", value="さらに探索"),
        Translation(key="EXPLORE_MORE", language="ZH", value="探索更多")
    ]

    content_node.translations = translations
    return await client.save_content_node(content_node)


async def create_user_name_value(client: ReactiveNodifyClient, content_node: ContentNode) -> ContentNode:
    """
    Step 5: Create USER_NAME value
    """
    user_name_value = Value(
        key="USER_NAME",
        value="John Doe"
    )

    content_node.values = [user_name_value]
    return await client.save_content_node(content_node)


async def publish_content(client: ReactiveNodifyClient, content_code: str) -> ContentNode:
    """
    Step 6: Publish the content
    """
    return await client.publish_content_node(content_code, True)


async def publish_node(client: ReactiveNodifyClient, node_code: str) -> Node:
    """
    Step 7: Publish the parent node
    """
    return await client.publish_node(node_code)


def display_final_info(parent_node: Node, child_node: Node, content_node: ContentNode) -> None:
    """
    Display final information about created resources
    """
    print("\n" + "=" * 60)
    print("🎯 FINAL SCENARIO SUMMARY")
    print("=" * 60)

    print("\n📁 PARENT NODE:")
    print(f"   - Name: {parent_node.name}")
    print(f"   - Code: {parent_node.code}")
    print(f"   - Default Language: {parent_node.default_language}")
    print(f"   - Slug: {parent_node.slug}")
    print(f"   - Parent: {parent_node.parent_code}")  # Should be None

    print("\n📄 CHILD NODE:")
    print(f"   - Name: {child_node.name}")
    print(f"   - Code: {child_node.code}")
    print(f"   - Parent: {child_node.parent_code}")  # Should be parent_node.code
    print(f"   - Slug: {child_node.slug}")

    print("\n📝 CONTENT NODE:")
    print(f"   - Code: {content_node.code}")
    print(f"   - Title: {content_node.title}")
    print(f"   - Parent: {content_node.parent_code}")  # Should be child_node.code

    print("\n🌍 TRANSLATIONS CREATED:")
    if content_node and content_node.translations:
        for t in content_node.translations:
            print(f"   - {t.key} [{t.language}]: {t.value}")
    else:
        print("   ❌ No translations found!")

    print("\n🔢 VALUES CREATED:")
    if content_node and content_node.values:
        for v in content_node.values:
            print(f"   - {v.key}: {v.value}")
    else:
        print("   ❌ No values found!")

    print("\n📊 HIERARCHY:")
    print(f"   {parent_node.name} ({parent_node.code}) [ROOT]")
    print(f"   └── {child_node.name} ({child_node.code})")
    print(f"       └── Content: {content_node.title} ({content_node.code})")

    print("\n🔍 HOW TO ACCESS:")
    print(f"   Parent Node: GET /v0/nodes/code/{parent_node.code}")
    print(f"   Child Node: GET /v0/nodes/code/{child_node.code}")
    print(f"   Content: GET /v0/content-node/code/{content_node.code}")

    print("\n" + "=" * 60)


# HTML Template
HTML_TEMPLATE = """
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
            <!-- Using $value directive for dynamic user name -->
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
            <p>Created with ❤️ using Nodify Python Client</p>
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
"""


if __name__ == "__main__":
    asyncio.run(main())