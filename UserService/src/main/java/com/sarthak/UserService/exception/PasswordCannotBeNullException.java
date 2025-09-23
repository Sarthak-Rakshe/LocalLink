package com.sarthak.UserService.exception;

public class PasswordCannotBeNullException extends RuntimeException {
    public PasswordCannotBeNullException(String message) {
        super(message);
    }
}
