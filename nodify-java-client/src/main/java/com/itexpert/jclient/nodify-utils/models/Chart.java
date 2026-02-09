package com.itexpert.jclient.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Chart implements Serializable, Cloneable {
    private String name;
    private String value;
    private boolean verified;
}
