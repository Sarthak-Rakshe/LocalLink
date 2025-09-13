package com.sarthak.UserService.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private String userName;
    private String userEmail;
    private String userContact;
    private String userType;

}
