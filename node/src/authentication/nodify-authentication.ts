// builder.ts

import { NodifyClientConfig } from '../models/nodify-models';
import { NodifyClient } from '../nodify-client';

export class NodifyClientBuilder {
  private config: Partial<NodifyClientConfig> = {
    timeout: 30000,
    defaultHeaders: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  };

  withBaseUrl(baseUrl: string): NodifyClientBuilder {
    this.config.baseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    return this;
  }

  withAuthToken(token: string): NodifyClientBuilder {
    this.config.authToken = token;
    return this;
  }

  withTimeout(timeout: number): NodifyClientBuilder {
    this.config.timeout = timeout;
    return this;
  }

  withDefaultHeaders(headers: Record<string, string>): NodifyClientBuilder {
    this.config.defaultHeaders = {
      ...this.config.defaultHeaders,
      ...headers
    };
    return this;
  }

  withHeader(key: string, value: string): NodifyClientBuilder {
    if (!this.config.defaultHeaders) {
      this.config.defaultHeaders = {};
    }
    this.config.defaultHeaders[key] = value;
    return this;
  }

  withAuthErrorHandler(handler: () => Promise<string | null>): NodifyClientBuilder {
    this.config.onAuthError = handler;
    return this;
  }

  build(): NodifyClient {
    if (!this.config.baseUrl) {
      throw new Error('Base URL is required');
    }

    return new NodifyClient(this.config as NodifyClientConfig);
  }
}