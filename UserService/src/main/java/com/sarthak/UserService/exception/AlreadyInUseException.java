package com.sarthak.UserService.exception;

public class AlreadyInUseException extends RuntimeException {
    public AlreadyInUseException(String message) {
        super(message);
    }
}
