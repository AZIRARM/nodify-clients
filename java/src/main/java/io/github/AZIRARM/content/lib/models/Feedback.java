package io.github.AZIRARM.content.lib.models;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class Feedback implements Serializable, Cloneable {
    private UUID id;
    private String contentCode;
    private Long createdDate;
    private Long modifiedDate;
    private int evaluation;
    private String message;
    private String userId;
    private boolean verified;
}
