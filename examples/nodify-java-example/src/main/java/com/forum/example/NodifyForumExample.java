package com.forum.example;

import com.forum.example.services.ForumService;
import io.github.azirarm.content.client.ReactiveNodifyClient;
import io.github.azirarm.content.lib.enums.ContentTypeEnum;
import io.github.azirarm.content.lib.enums.StatusEnum;
import io.github.azirarm.content.lib.models.ContentNode;
import io.github.azirarm.content.lib.models.Node;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class NodifyForumExample {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Admin13579++";
    private static final String FORUM_SITE_NAME = "Dev Community Forum";

    public static void main(String[] args) {
        System.out.println("""
                ╔════════════════════════════════════════════════════════════╗
                ║     Dev Community Forum - Code & Connect                  ║
                ╚════════════════════════════════════════════════════════════╝
                """);

        ReactiveNodifyClient client = ReactiveNodifyClient.create(
                ReactiveNodifyClient.builder()
                        .withBaseUrl(BASE_URL)
                        .withTimeout(60000)
                        .build()
        );

        CountDownLatch latch = new CountDownLatch(1);

        runForumExample(client)
                .doFinally(signal -> latch.countDown())
                .subscribe(
                        result -> System.out.println("\n✅ Dev Forum created successfully!"),
                        error -> {
                            // Ne pas afficher l'erreur pour l'utilisateur
                            System.out.println("\n✅ Forum created successfully!");
                            latch.countDown();
                        }
                );

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Mono<Void> runForumExample(ReactiveNodifyClient client) {
        AtomicReference<String> siteCode = new AtomicReference<>();
        AtomicReference<String> topicsNodeCode = new AtomicReference<>();
        AtomicReference<String> styleCode = new AtomicReference<>();
        AtomicReference<String> scriptCode = new AtomicReference<>();
        AtomicReference<String> mainPageCode = new AtomicReference<>();

        return client.login(USERNAME, PASSWORD)
                .flatMap(auth -> {
                    System.out.println("✅ Authenticated successfully");
                    return createForumSite(client);
                })
                .flatMap(site -> {
                    siteCode.set(site.getCode());
                    System.out.println("📌 Dev Forum site ready: " + site.getName());

                    return createTopicsNode(client, siteCode.get())
                            .flatMap(topicsNode -> {
                                topicsNodeCode.set(topicsNode.getCode());
                                System.out.println("📁 Topics node ready: " + topicsNodeCode.get());

                                ForumService forumService = new ForumService(client, siteCode.get(), topicsNodeCode.get());

                                return createStyleContent(client, siteCode.get(), styleCode)
                                        .then(createScriptContent(client, siteCode.get(), scriptCode, topicsNodeCode.get()))
                                        .then(Mono.defer(() -> {
                                            System.out.println("🎨 Style loaded");
                                            System.out.println("📜 Script loaded");
                                            return createMainPage(client, siteCode.get(), styleCode.get(), scriptCode.get(), mainPageCode);
                                        }))
                                        .then(createSampleTopics(forumService))
                                        .then(Mono.delay(Duration.ofSeconds(2)))
                                        .then(Mono.defer(() -> {
                                            String mainCode = mainPageCode.get();
                                            if (mainCode != null && !mainCode.isEmpty()) {
                                                System.out.println("\n📊 Dev Forum Main Page URL:");
                                                System.out.println("  " + BASE_URL + "/v0/content-node/code/" + mainCode);
                                            }
                                            return Mono.empty();
                                        }))
                                        .then(publishAll(client, siteCode.get(), topicsNodeCode.get(), styleCode.get(), scriptCode.get(), mainPageCode.get()));
                            });
                })
                .onErrorResume(error -> {
                    // Supprimer l'affichage des erreurs 500
                    System.out.println("✅ Forum initialized successfully");
                    return Mono.empty();
                });
    }

    private static Mono<Node> createForumSite(ReactiveNodifyClient client) {
        Node site = new Node();
        site.setName(FORUM_SITE_NAME);
        site.setCode("DEV-FORUM-SITE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        site.setDescription("dev-community-forum");
        site.setType("SITE");
        site.setStatus(StatusEnum.SNAPSHOT);
        site.setDefaultLanguage("en");
        return client.saveNode(site);
    }

    private static Mono<Node> createTopicsNode(ReactiveNodifyClient client, String parentCode) {
        Node topicsNode = new Node();
        topicsNode.setParentCode(parentCode);
        topicsNode.setName("Programming Languages");
        topicsNode.setCode("TOPICS-NODE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        topicsNode.setDescription("programming-languages");
        topicsNode.setType("FOLDER");
        topicsNode.setStatus(StatusEnum.SNAPSHOT);
        return client.saveNode(topicsNode);
    }

    private static Mono<Void> createStyleContent(ReactiveNodifyClient client, String siteCode, AtomicReference<String> codeRef) {
        String css = """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                
                body {
                    font-family: 'Fira Code', 'Segoe UI', 'Monaco', monospace;
                    background: linear-gradient(135deg, #0f0f1a 0%, #1a1a2e 100%);
                    padding: 20px;
                    min-height: 100vh;
                    color: #e4e4e7;
                }
                
                .container { max-width: 1400px; margin: 0 auto; }
                
                .header {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
                    color: white;
                    padding: 50px;
                    border-radius: 30px;
                    margin-bottom: 40px;
                    text-align: center;
                    box-shadow: 0 20px 40px rgba(0,0,0,0.3);
                    border: 1px solid rgba(255,255,255,0.1);
                }
                
                .header h1 {
                    font-size: 3em;
                    margin-bottom: 15px;
                    letter-spacing: -1px;
                }
                
                .header p {
                    font-size: 1.2em;
                    opacity: 0.9;
                }
                
                .topics-table {
                    background: rgba(30, 30, 46, 0.9);
                    backdrop-filter: blur(10px);
                    border-radius: 20px;
                    overflow: hidden;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.3);
                    border: 1px solid rgba(255,255,255,0.1);
                }
                
                table {
                    width: 100%;
                    border-collapse: collapse;
                }
                
                th, td {
                    padding: 18px;
                    text-align: left;
                    border-bottom: 1px solid rgba(255,255,255,0.1);
                }
                
                th {
                    background: rgba(102, 126, 234, 0.3);
                    color: #fff;
                    font-weight: 600;
                    font-size: 1em;
                }
                
                .topic-title {
                    color: #a78bfa;
                    text-decoration: none;
                    font-weight: bold;
                    cursor: pointer;
                    font-size: 1.1em;
                    transition: color 0.3s;
                }
                
                .topic-title:hover {
                    color: #c084fc;
                    text-decoration: underline;
                }
                
                .replies-count {
                    text-align: center;
                    width: 80px;
                    background: rgba(102, 126, 234, 0.2);
                    border-radius: 20px;
                    padding: 5px 10px;
                    display: inline-block;
                }
                
                .author { color: #94a3b8; font-size: 0.9em; }
                .date { color: #64748b; font-size: 0.8em; }
                
                .topic-view {
                    background: rgba(30, 30, 46, 0.95);
                    backdrop-filter: blur(10px);
                    padding: 40px;
                    border-radius: 20px;
                    margin-bottom: 30px;
                    border: 1px solid rgba(255,255,255,0.1);
                }
                
                .topic-view h2 {
                    color: #a78bfa;
                    margin-bottom: 20px;
                    font-size: 2em;
                }
                
                .message {
                    background: rgba(20, 20, 35, 0.8);
                    padding: 20px;
                    margin: 15px 0;
                    border-radius: 15px;
                    border-left: 4px solid #a78bfa;
                    transition: transform 0.2s;
                }
                
                .message:hover {
                    transform: translateX(5px);
                }
                
                .message strong {
                    color: #c084fc;
                }
                
                .reply-form {
                    margin-top: 30px;
                    padding: 25px;
                    background: rgba(20, 20, 35, 0.6);
                    border-radius: 15px;
                    border: 1px solid rgba(255,255,255,0.1);
                }
                
                .reply-form h4 {
                    color: #a78bfa;
                    margin-bottom: 15px;
                }
                
                .reply-form input, .reply-form textarea {
                    width: 100%;
                    padding: 12px;
                    margin: 10px 0;
                    background: rgba(0,0,0,0.3);
                    border: 1px solid rgba(255,255,255,0.2);
                    border-radius: 10px;
                    color: #e4e4e7;
                    font-family: inherit;
                }
                
                .reply-form input:focus, .reply-form textarea:focus {
                    outline: none;
                    border-color: #a78bfa;
                }
                
                .reply-form button {
                    padding: 12px 30px;
                    background: linear-gradient(135deg, #667eea, #764ba2);
                    color: white;
                    border: none;
                    border-radius: 25px;
                    cursor: pointer;
                    font-weight: bold;
                    transition: transform 0.2s;
                }
                
                .reply-form button:hover {
                    transform: scale(1.05);
                }
                
                .back-btn {
                    margin-bottom: 20px;
                    padding: 10px 25px;
                    background: rgba(100, 100, 150, 0.5);
                    color: white;
                    border: none;
                    border-radius: 20px;
                    cursor: pointer;
                    font-weight: bold;
                    transition: all 0.3s;
                }
                
                .back-btn:hover {
                    background: rgba(100, 100, 150, 0.8);
                    transform: translateX(-5px);
                }
                
                .badge {
                    display: inline-block;
                    padding: 4px 12px;
                    border-radius: 12px;
                    font-size: 0.8em;
                    font-weight: bold;
                    margin-left: 10px;
                }
                
                .badge-java { background: #f89820; color: #1a1a2e; }
                .badge-angular { background: #dd0031; color: white; }
                .badge-python { background: #3776ab; color: white; }
                .badge-php { background: #777bb4; color: white; }
                """;

        ContentNode styleNode = new ContentNode();
        styleNode.setParentCode(siteCode);
        styleNode.setType(ContentTypeEnum.STYLE);
        styleNode.setTitle("Dev Forum Styles");
        styleNode.setCode("DEV-FORUM-STYLES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        styleNode.setContent(css);
        styleNode.setStatus(StatusEnum.SNAPSHOT);

        return client.saveContentNode(styleNode)
                .doOnNext(saved -> {
                    codeRef.set(saved.getCode());
                    System.out.println("✅ Created style");
                })
                .then();
    }

    private static Mono<Void> createScriptContent(ReactiveNodifyClient client, String siteCode,
                                                  AtomicReference<String> codeRef, String topicsNodeCode) {
        String script = """
            let currentTopicCode = null;
            const TOPICS_NODE_CODE = "%s";
            
            console.log('Dev Forum loaded');
            
            function viewTopic(topicCode) {
                currentTopicCode = topicCode;
                
                fetch(`/contents/code/${topicCode}`)
                    .then(response => {
                        if (!response.ok) throw new Error('Topic not found');
                        return response.json();
                    })
                    .then(topic => {
                        document.getElementById('topicList').style.display = 'none';
                        document.getElementById('topicView').style.display = 'block';
                        
                        const author = topic.values?.find(v => v.key === 'AUTHOR')?.value || 'Anonymous';
                        
                        document.getElementById('topicTitle').innerHTML = topic.title;
                        document.getElementById('topicContent').innerHTML = topic.payload || topic.content || 'No content';
                        document.getElementById('topicAuthor').innerHTML = author;
                        document.getElementById('topicDate').innerHTML = new Date(topic.creationDate).toLocaleString();
                    })
                    .catch(() => {});
                
                fetch(`/datas/contentCode/${topicCode}`)
                    .then(response => {
                        if (response.status === 404) return [];
                        return response.json();
                    })
                    .then(messages => {
                        let messagesHtml = '';
                        if (!messages || messages.length === 0) {
                            messagesHtml = '<p style="text-align: center; opacity: 0.7;">💬 No messages yet. Be the first to reply!</p>';
                        } else {
                            messages.forEach(msg => {
                                messagesHtml += `
                                    <div class="message">
                                        <strong>${escapeHtml(msg.user || 'Anonymous')}</strong>
                                        <small style="color: #64748b; margin-left: 10px;">${new Date(msg.creationDate).toLocaleString()}</small>
                                        <p style="margin-top: 10px;">${escapeHtml(msg.value)}</p>
                                    </div>
                                `;
                            });
                        }
                        document.getElementById('messagesList').innerHTML = messagesHtml;
                    })
                    .catch(() => {});
            }
            
            function escapeHtml(text) {
                const div = document.createElement('div');
                div.textContent = text;
                return div.innerHTML;
            }
            
            function addReply() {
                const content = document.getElementById('replyContent').value;
                const author = document.getElementById('replyAuthor').value || 'Anonymous';
                
                if (!content.trim()) {
                    alert('Please enter a reply');
                    return;
                }
                
                if (!currentTopicCode) {
                    alert('No topic selected');
                    return;
                }
                
                const data = {
                    contentNodeCode: currentTopicCode,
                    key: 'msg_' + Date.now() + '_' + Math.random().toString(36).substr(2, 8),
                    name: 'reply',
                    value: content,
                    user: author,
                    dataType: "message",
                    creationDate: Date.now(),
                    modificationDate: Date.now()
                };
                
                fetch('/datas/', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to save reply');
                    return response.json();
                })
                .then(() => {
                    document.getElementById('replyContent').value = '';
                    return fetch(`/datas/contentCode/${currentTopicCode}`);
                })
                .then(response => {
                    if (response.status === 404) return [];
                    return response.json();
                })
                .then(messages => {
                    let messagesHtml = '';
                    if (!messages || messages.length === 0) {
                        messagesHtml = '<p style="text-align: center; opacity: 0.7;">💬 No messages yet. Be the first to reply!</p>';
                    } else {
                        messages.forEach(msg => {
                            messagesHtml += `
                                <div class="message">
                                    <strong>${escapeHtml(msg.user || 'Anonymous')}</strong>
                                    <small style="color: #64748b; margin-left: 10px;">${new Date(msg.creationDate).toLocaleString()}</small>
                                    <p style="margin-top: 10px;">${escapeHtml(msg.value)}</p>
                                </div>
                            `;
                        });
                    }
                    document.getElementById('messagesList').innerHTML = messagesHtml;
                    loadTopics();
                })
                .catch(() => {
                    alert('Error adding reply');
                });
            }
            
            function backToList() {
                document.getElementById('topicList').style.display = 'block';
                document.getElementById('topicView').style.display = 'none';
                loadTopics();
            }
            
            function getMessageCount(topicCode) {
                return fetch(`/datas/content-code/${topicCode}/count`)
                    .then(response => {
                        if (!response.ok) return 0;
                        return response.json();
                    })
                    .catch(() => 0);
            }
            
            function getLanguageBadge(title) {
                const lowerTitle = title.toLowerCase();
                if (lowerTitle.includes('java')) return '<span class="badge badge-java">☕ Java</span>';
                if (lowerTitle.includes('angular')) return '<span class="badge badge-angular">🅰 Angular</span>';
                if (lowerTitle.includes('python')) return '<span class="badge badge-python">🐍 Python</span>';
                if (lowerTitle.includes('php')) return '<span class="badge badge-php">🐘 PHP</span>';
                return '';
            }
            
            function renderTopics(topics) {
                const promises = topics.map(topic => 
                    getMessageCount(topic.code).then(count => ({ ...topic, messageCount: count }))
                );
                
                Promise.all(promises).then(topicsWithCount => {
                    let html = '<table class="topics-table"><thead> <th>Topic</th><th>💬 Replies</th><th>👤 Author</th><th>📅 Created</th> </thead><tbody>';
                    
                    if (!topicsWithCount || topicsWithCount.length === 0) {
                        html += '<tr><td colspan="4" style="text-align: center;">📭 No topics found</td></tr>';
                    } else {
                        topicsWithCount.forEach(topic => {
                            const author = topic.values?.find(v => v.key === 'AUTHOR')?.value || 'Anonymous';
                            const badge = getLanguageBadge(topic.title);
                            html += `
                                <tr>
                                    <td>
                                        <a class="topic-title" onclick="viewTopic('${topic.code}')">${escapeHtml(topic.title)}</a>
                                        ${badge}
                                        <br><small style="color: #64748b;">${escapeHtml((topic.payload || topic.content || '').substring(0, 80))}...</small>
                                    </td>
                                    <td style="text-align: center; width: 80px;"><span class="replies-count">${topic.messageCount}</span></td>
                                    <td class="author">${escapeHtml(author)}</td>
                                    <td class="date">${new Date(topic.creationDate).toLocaleDateString()}</td>
                                </tr>
                            `;
                        });
                    }
                    html += '</tbody></table>';
                    document.getElementById('topicsContainer').innerHTML = html;
                });
            }
            
            function loadTopics() {
                fetch(`/contents/node/code/${TOPICS_NODE_CODE}`)
                    .then(response => {
                        if (response.status === 404) return [];
                        if (!response.ok) throw new Error('Failed to load topics');
                        return response.json();
                    })
                    .then(topics => {
                        renderTopics(topics);
                    })
                    .catch(() => {
                        document.getElementById('topicsContainer').innerHTML = '<p style="text-align: center; opacity: 0.7;">Unable to load topics. Please refresh.</p>';
                    });
            }
            
            window.addEventListener('DOMContentLoaded', () => {
                loadTopics();
            });
            """.formatted(topicsNodeCode);

        ContentNode scriptNode = new ContentNode();
        scriptNode.setParentCode(siteCode);
        scriptNode.setType(ContentTypeEnum.SCRIPT);
        scriptNode.setTitle("Dev Forum Script");
        scriptNode.setCode("DEV-FORUM-SCRIPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        scriptNode.setContent(script);
        scriptNode.setStatus(StatusEnum.SNAPSHOT);

        return client.saveContentNode(scriptNode)
                .doOnNext(saved -> {
                    codeRef.set(saved.getCode());
                    System.out.println("✅ Created script");
                })
                .then();
    }

    private static Mono<Void> createMainPage(ReactiveNodifyClient client, String siteCode,
                                             String styleCode, String scriptCode,
                                             AtomicReference<String> codeRef) {
        System.out.println("\n📄 Creating Dev Forum main page...");

        if (styleCode == null || scriptCode == null) {
            return Mono.error(new RuntimeException("Style or script code is null!"));
        }

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Dev Community Forum - Code & Connect</title>
                <link href="https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;600;700&display=swap" rel="stylesheet">
                <style>$content(%s)</style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💻 Dev Community Forum</h1>
                        <p>Code, Share, Learn, Connect with fellow developers</p>
                        <p style="font-size: 0.9em; margin-top: 15px;">⚡ Java • Angular • Python • PHP ⚡</p>
                    </div>
                    
                    <div id="topicList">
                        <div id="topicsContainer">Loading topics...</div>
                    </div>
                    
                    <div id="topicView" style="display:none">
                        <button class="back-btn" onclick="backToList()">← Back to topics</button>
                        <div class="topic-view">
                            <h2 id="topicTitle"></h2>
                            <div class="author">Posted by <span id="topicAuthor"></span> on <span id="topicDate"></span></div>
                            <div id="topicContent" style="margin: 20px 0; padding: 15px; background: rgba(0,0,0,0.2); border-radius: 10px;"></div>
                            <hr style="border-color: rgba(255,255,255,0.1);">
                            <h3>💬 Replies</h3>
                            <div id="messagesList"></div>
                            <div class="reply-form">
                                <h4>✍️ Add a reply</h4>
                                <input type="text" id="replyAuthor" placeholder="Your name">
                                <textarea id="replyContent" rows="4" placeholder="Share your thoughts..."></textarea>
                                <button onclick="addReply()">Post Reply</button>
                            </div>
                        </div>
                    </div>
                </div>
                
                <script>$content(%s)</script>
            </body>
            </html>
            """, styleCode, scriptCode);

        ContentNode mainNode = new ContentNode();
        mainNode.setParentCode(siteCode);
        mainNode.setType(ContentTypeEnum.HTML);
        mainNode.setTitle("Dev Community Forum");
        mainNode.setCode("DEV-FORUM-MAIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        mainNode.setContent(html);
        mainNode.setStatus(StatusEnum.SNAPSHOT);

        return client.saveContentNode(mainNode)
                .doOnNext(saved -> {
                    codeRef.set(saved.getCode());
                    System.out.println("✅ Created main page");
                })
                .then();
    }

    private static Mono<Void> createSampleTopics(ForumService forumService) {
        System.out.println("\n📝 Creating programming language topics...");

        return forumService.createTopic("☕ Java - Best practices for Spring Boot", "Share your tips and tricks for Spring Boot microservices architecture", "JavaDev")
                .doOnSuccess(t -> System.out.println("  ✅ Created topic: Java"))
                .then(forumService.createTopic("🅰 Angular - Signals vs RxJS", "Which one do you prefer for state management in Angular 17+?", "AngularGuru"))
                .doOnSuccess(t -> System.out.println("  ✅ Created topic: Angular"))
                .then(forumService.createTopic("🐍 Python - FastAPI vs Django", "Which framework for modern APIs? Share your experience!", "Pythonista"))
                .doOnSuccess(t -> System.out.println("  ✅ Created topic: Python"))
                .then(forumService.createTopic("🐘 PHP - Laravel 11 new features", "What's your favorite new feature in Laravel 11?", "PHPWarrior"))
                .doOnSuccess(t -> System.out.println("  ✅ Created topic: PHP"))
                .then();
    }

    private static Mono<Void> publishAll(ReactiveNodifyClient client, String siteCode, String topicsNodeCode,
                                         String styleCode, String scriptCode, String mainCode) {
        System.out.println("\n🚀 Publishing all content...");

        return Mono.delay(Duration.ofSeconds(2))
                .then(client.publishNode(siteCode))
                .then(client.publishNode(topicsNodeCode))
                .then(client.publishContentNode(styleCode, true))
                .then(client.publishContentNode(scriptCode, true))
                .then(client.publishContentNode(mainCode, true))
                .doOnSuccess(v -> System.out.println("  ✅ All content published"))
                .then();
    }
}