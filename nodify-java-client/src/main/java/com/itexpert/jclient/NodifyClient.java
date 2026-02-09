package com.itexpert.jclient;

import org.springframework.web.reactive.function.client.WebClient;

import com.itexpert.jclient.flow.NodeExistenceFlow;
import com.itexpert.jclient.http.NodifyWebClient;

public class NodifyClient {

    private final String baseUrl;
    private final String apiKey;
    private final WebClient webClient;
    private final NodifyWebClient nodifyWebClient;

    private NodifyClient(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.apiKey = builder.apiKey;

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        this.nodifyWebClient = new NodifyWebClient(webClient);
    }

    public static Builder builder() {
        return new Builder();
    }

    // Entry point fluent API
    public NodeExistenceFlow checkIfNodeExist(String nodeCode) {
        return new NodeExistenceFlow(nodeCode, nodifyWebClient);
    }

    // ===== BUILDER =====
    public static class Builder {

        private String baseUrl;
        private String apiKey;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public NodifyClient build() {
            if (baseUrl == null || apiKey == null) {
                throw new IllegalStateException("baseUrl and apiKey are required");
            }
            return new NodifyClient(this);
        }
    }
}
