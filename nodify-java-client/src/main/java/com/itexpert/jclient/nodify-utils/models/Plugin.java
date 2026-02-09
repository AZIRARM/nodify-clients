package com.itexpert.jclient.models;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class Plugin implements Serializable, Cloneable {

    private UUID id;

    private boolean enabled;

    private boolean editable;

    private String description;

    private String name;

    private String code;

    private String entrypoint;


    private Long creationDate;
    private Long modificationDate;
    private String modifiedBy;

    private boolean deleted;

    private List<PluginFile> resources;
}
