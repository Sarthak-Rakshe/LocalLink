package com.sarthak.UserService.exception;

import com.sarthak.UserService.dto.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ExceptionResponse body = new ExceptionResponse("User Not Found", ex.getMessage(), "404");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
