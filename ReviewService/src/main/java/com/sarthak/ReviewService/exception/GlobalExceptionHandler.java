package com.sarthak.ReviewService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ExceptionResponse(String error, String message, long timestamp, int status){}

    @ExceptionHandler(DuplicateReviewForSameServiceException.class)
    public ExceptionResponse handleDuplicateReview(DuplicateReviewForSameServiceException ex){
        return new ExceptionResponse("Duplicate Entity", ex.getMessage(), System.currentTimeMillis(), HttpStatus.CONFLICT.value());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ExceptionResponse handleEntityNotFound(EntityNotFoundException ex){
        return new ExceptionResponse("Entity Not Found", ex.getMessage(), System.currentTimeMillis(), HttpStatus.NOT_FOUND.value());
    }

}
