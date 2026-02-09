package com.itexpert.jclient.models;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;

@Data
public class ContentClick implements Serializable, Cloneable {
    private UUID id;
    private String contentCode;
    private Long clicks;
}
