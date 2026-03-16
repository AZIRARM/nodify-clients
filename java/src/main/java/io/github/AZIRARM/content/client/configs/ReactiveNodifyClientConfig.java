package io.github.AZIRARM.content.client.configs;

import java.util.HashMap;
import java.util.Map;

public class ReactiveNodifyClientConfig {
    private final String baseUrl;
    private final int timeout;
    private final Map<String, String> defaultHeaders;
    private String authToken;

    private ReactiveNodifyClientConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.timeout = builder.timeout;
        this.defaultHeaders = builder.defaultHeaders;
        this.authToken = builder.authToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public static class Builder {
        private String baseUrl;
        private int timeout = 30000;
        private Map<String, String> defaultHeaders = new HashMap<>();
        private String authToken;

        public Builder() {
            defaultHeaders.put("Content-Type", "application/json");
            defaultHeaders.put("Accept", "application/json");
        }

        public Builder withBaseUrl(String baseUrl) {
            if (baseUrl.endsWith("/")) {
                this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            } else {
                this.baseUrl = baseUrl;
            }
            return this;
        }

        public Builder withTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder withAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public Builder withHeader(String key, String value) {
            this.defaultHeaders.put(key, value);
            return this;
        }

        public ReactiveNodifyClientConfig build() {
            if (baseUrl == null || baseUrl.isEmpty()) {
                throw new IllegalArgumentException("Base URL is required");
            }
            return new ReactiveNodifyClientConfig(this);
        }
    }
}