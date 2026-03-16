package io.github.AZIRARM.content.lib.models;

import io.github.AZIRARM.content.lib.enums.OperatorEnum;
import io.github.AZIRARM.content.lib.enums.TypeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class Rule implements Serializable, Cloneable {


    private TypeEnum type;
    private String name;
    private String value;
    private boolean editable;
    private boolean erasable;
    private OperatorEnum operator;

    private Boolean behavior;
    private Boolean enable;

    private String description;

    public Rule clone() throws CloneNotSupportedException {
        return (Rule) super.clone();
    }
}
