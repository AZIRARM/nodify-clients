package io.github.azirarm.content.lib.models;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class PluginFile implements Serializable, Cloneable {
    private UUID id;

    private UUID pluginId;

    private String fileName;

    private String description;

    private String data;
}
