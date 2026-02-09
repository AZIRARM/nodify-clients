package com.itexpert.jclient.models;

import com.itexpert.jclient.enums.BehaviorEnum;
import com.itexpert.jclient.enums.OperatorEnum;
import com.itexpert.jclient.enums.TypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

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
