package com.blog.example.models;

/**
 * Author record representing a blog author
 * Using Java 17 record feature for immutable data
 */
public record Author(
        String name,
        String email,
        String bio,
        String avatarUrl
) {
    @Override
    public String toString() {
        return String.format("Author{name='%s', email='%s'}", name, email);
    }
}