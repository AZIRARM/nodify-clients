package com.itexpert.jclient.http;

import org.springframework.web.reactive.function.client.WebClient;

import com.itexpert.jclient.models.Node;

import reactor.core.publisher.Mono;

public class NodifyWebClient {

    private final WebClient webClient;

    public NodifyWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Node> getNodeByCode(String code) {
        return webClient.get()
                .uri("/nodes/code/{code}", code)
                .retrieve()
                .bodyToMono(Node.class)
                .onErrorResume(e -> Mono.empty()); // si 404
    }

    public Mono<Node> saveNode(Node node) {
        return webClient.post()
                .uri("/nodes")
                .bodyValue(node)
                .retrieve()
                .bodyToMono(Node.class);
    }

    public Mono<Void> publishNode(String nodeId) {
        return webClient.post()
                .uri("/nodes/{id}/publish", nodeId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
