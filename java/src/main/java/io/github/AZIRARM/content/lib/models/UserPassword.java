package io.github.AZIRARM.content.lib.models;

import lombok.Data;

@Data
public class UserPassword {

    private String userId;

    private String password;

    private String newPassword;

}
