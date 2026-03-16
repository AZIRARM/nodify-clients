package io.github.AZIRARM.content.lib.models;

import lombok.Data;

import java.util.UUID;

@Data
public class User {

    private UUID id;

    private String firstname;

    private String lastname;
}
