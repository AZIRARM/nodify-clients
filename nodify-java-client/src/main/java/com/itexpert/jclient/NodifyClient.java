package com.itexpert.jclient;

import java.util.function.Consumer;

import com.itexpert.jclient.models.Node;

public class NodifyClient {
    private String url;
    private String apiKey;
    private boolean exists = false;
    private Node node;

    // Private constructor to enforce the builder pattern
    private NodifyClient(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
    }

    public static class Builder {
        private String url;
        private String apiKey;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public NodifyClient build() {
            if (url == null || apiKey == null) {
                throw new IllegalArgumentException("URL and API key must be provided");
            }
            return new NodifyClient(url, apiKey);
        }
    }

    public NodifyClient checkIfNodeExist(String codeNode) {
        // Simulate checking if node exists
        this.exists = true; // Replace with actual API call to check existence
        return this;
    }

    public NodifyClient ifExists(Consumer<Node> action) {
        if (exists) {
            action.accept(node);
        }
        return this;
    }

    public NodifyClient ifNotExists(Runnable action) {
        if (!exists) {
            action.run();
        }
        return this;
    }

    public NodifyClient save() {
        // Simulate saving node
        System.out.println("Node saved");
        return this;
    }

    public NodifyClient publish() {
        // Simulate publishing node
        System.out.println("Node published");
        return this;
    }
}