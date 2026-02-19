// exceptions/ReactiveNodifyClientException.java
package com.itexpert.content.client.exceptions;

public class ReactiveNodifyClientException extends RuntimeException {
    private final int statusCode;

    public ReactiveNodifyClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ReactiveNodifyClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public int getStatusCode() {
        return statusCode;
    }
}