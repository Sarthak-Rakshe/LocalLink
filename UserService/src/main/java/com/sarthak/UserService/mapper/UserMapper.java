package com.sarthak.UserService.mapper;

import com.sarthak.UserService.model.User;
import com.sarthak.UserService.request.UserRequest;
import com.sarthak.UserService.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse UserToUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setUserName(user.getUserName());
        userResponse.setUserContact(user.getUserContact());
        userResponse.setUserEmail(user.getUserEmail());
        userResponse.setUserType(user.getUserType().name());

        return userResponse;
    }

    public User UserResponseToUser(UserResponse userResponse){
        User user = new User();
        user.setUserName(userResponse.getUserName());
        user.setUserContact(userResponse.getUserContact());
        user.setUserEmail(userResponse.getUserEmail());
        user.setUserType(User.Types.valueOf(userResponse.getUserType()));

        return user;
    }

    public User UserRequestToUser(UserRequest userRequest){
        User user = new User();
        user.setUserName(userRequest.getUserName());
        user.setUserContact(userRequest.getUserContact());
        user.setUserEmail(userRequest.getUserEmail());
        user.setUserType(User.Types.valueOf(userRequest.getUserType()));
        user.setUserPassword(userRequest.getUserPassword());
        return user;
    }

}
