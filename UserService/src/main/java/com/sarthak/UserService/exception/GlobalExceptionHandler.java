package com.sarthak.UserService.exception;

import com.sarthak.UserService.response.ExceptionResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Component
public class GlobalExceptionHandler {

    @ExceptionHandler
    public  ExceptionResponse handleUserNotFoundException(UserNotFoundException ex) {
        return new ExceptionResponse("User Not Found", ex.getMessage(), "404");
    }
}
