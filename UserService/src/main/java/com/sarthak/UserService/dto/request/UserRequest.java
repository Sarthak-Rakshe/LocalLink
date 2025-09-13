package com.sarthak.UserService.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    private String userName;
    private String userEmail;
    private String userContact;
    private String userType;
    private String userPassword;
}
