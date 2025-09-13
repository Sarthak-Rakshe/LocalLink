package com.sarthak.ReviewService.exception;

public class DuplicateReviewForSameServiceException extends RuntimeException {
    public DuplicateReviewForSameServiceException(String message) {
        super(message);
    }
}
