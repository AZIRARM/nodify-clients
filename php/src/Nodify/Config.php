<?php

namespace Nodify;

class Config
{
    private string $baseUrl;
    private int $timeout;
    private array $defaultHeaders;
    private ?string $authToken = null;

    public function __construct(string $baseUrl, int $timeout = 30000, array $defaultHeaders = [], ?string $authToken = null)
    {
        $this->baseUrl = rtrim($baseUrl, '/');
        $this->timeout = $timeout;
        $this->defaultHeaders = array_merge([
            'Content-Type' => 'application/json',
            'Accept' => 'application/json'
        ], $defaultHeaders);
        $this->authToken = $authToken;
    }

    public function getBaseUrl(): string
    {
        return $this->baseUrl;
    }

    public function getTimeout(): int
    {
        return $this->timeout;
    }

    public function getDefaultHeaders(): array
    {
        return $this->defaultHeaders;
    }

    public function getAuthToken(): ?string
    {
        return $this->authToken;
    }

    public function setAuthToken(?string $token): void
    {
        $this->authToken = $token;
    }

    public static function builder(): ConfigBuilder
    {
        return new ConfigBuilder();
    }
}

class ConfigBuilder
{
    private ?string $baseUrl = null;
    private int $timeout = 30000;
    private array $defaultHeaders = [];
    private ?string $authToken = null;

    public function __construct()
    {
        $this->defaultHeaders = [
            'Content-Type' => 'application/json',
            'Accept' => 'application/json'
        ];
    }

    public function withBaseUrl(string $baseUrl): self
    {
        $this->baseUrl = $baseUrl;
        return $this;
    }

    public function withTimeout(int $timeout): self
    {
        $this->timeout = $timeout;
        return $this;
    }

    public function withAuthToken(?string $authToken): self
    {
        $this->authToken = $authToken;
        return $this;
    }

    public function withHeader(string $key, string $value): self
    {
        $this->defaultHeaders[$key] = $value;
        return $this;
    }

    public function build(): Config
    {
        if (!$this->baseUrl) {
            throw new \InvalidArgumentException("Base URL is required");
        }

        return new Config(
            $this->baseUrl,
            $this->timeout,
            $this->defaultHeaders,
            $this->authToken
        );
    }
}