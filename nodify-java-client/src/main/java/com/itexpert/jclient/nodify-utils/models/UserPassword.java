package com.itexpert.jclient.models;

import lombok.Data;

@Data
public class UserPassword {

    private String userId;

    private String password;

    private String newPassword;

}
