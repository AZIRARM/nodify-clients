package com.itexpert.jclient.models;

import com.itexpert.jclient.enums.UrlTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class ContentFile implements Serializable, Cloneable {

    private String name;

    private String type;

    private String data;

    private int size;

    public ContentFile clone() throws CloneNotSupportedException {
        return (ContentFile) super.clone();
    }
}
