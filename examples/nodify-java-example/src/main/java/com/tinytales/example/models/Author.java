    package com.tinytales.example.models;

    /**
     * Author record for story authors
     */
    public record Author(
            String name,
            String email,
            String bio,
            String avatarUrl
    ) {
        @Override
        public String toString() {
            return name;
        }
    }
