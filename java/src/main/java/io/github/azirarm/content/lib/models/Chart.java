package io.github.azirarm.content.lib.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Chart implements Serializable, Cloneable {
    private String name;
    private String value;
    private boolean verified;
}
