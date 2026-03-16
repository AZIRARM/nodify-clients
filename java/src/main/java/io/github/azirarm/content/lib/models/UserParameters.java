package io.github.azirarm.content.lib.models;

import lombok.Data;

import java.util.UUID;

@Data
public class UserParameters {

    private UUID id;

    private UUID userId;

    private boolean acceptNotifications;
    private String defaultLanguage;
    private String theme;
    private boolean ai;
}
