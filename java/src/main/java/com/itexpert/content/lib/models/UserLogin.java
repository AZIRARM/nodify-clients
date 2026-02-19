package com.itexpert.content.lib.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLogin {

    private String email;

    private String password;

}
