package com.sarthak.ReviewService.exception;

public class BookingNotCompleteException extends RuntimeException {
    public BookingNotCompleteException(String message) {
        super(message);
    }
}
