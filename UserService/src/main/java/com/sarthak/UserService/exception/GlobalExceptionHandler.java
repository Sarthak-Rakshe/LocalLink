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

    @ExceptionHandler(AlreadyInUseException.class)
    public ResponseEntity<ExceptionResponse> handleAlreadyInUseException(AlreadyInUseException ex) {
        ExceptionResponse body = new ExceptionResponse("Already In Use", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PasswordCannotBeNullException.class)
    public ResponseEntity<ExceptionResponse> handlePasswordCannotBeNullException(PasswordCannotBeNullException ex) {
        ExceptionResponse body = new ExceptionResponse("Password Cannot Be Null", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidUserTypeException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidUserTypeException(InvalidUserTypeException ex) {
        ExceptionResponse body = new ExceptionResponse("Invalid User Type", ex.getMessage(), "400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ExceptionResponse body = new ExceptionResponse("Invalid Credentials", ex.getMessage(), "401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        ExceptionResponse body = new ExceptionResponse("Internal Server Error", ex.getMessage(), "500");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
