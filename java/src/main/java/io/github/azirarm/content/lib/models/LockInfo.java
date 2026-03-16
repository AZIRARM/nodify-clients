package io.github.azirarm.content.lib.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LockInfo {
    private String owner;
    private Boolean mine;
    private Boolean locked;
    private String resourceCode;
}

