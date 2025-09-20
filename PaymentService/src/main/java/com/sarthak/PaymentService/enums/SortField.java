package com.sarthak.PaymentService.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum SortField {
    TRANSACTION_ID("transactionId"),
    BOOKING_ID("bookingId"),
    CUSTOMER_ID("customerId"),
    AMOUNT("amount"),
    PAYMENT_METHOD("paymentMethod"),
    PAYMENT_STATUS("paymentStatus"),
    TRANSACTION_REFERENCE("transactionReference"),
    CREATED_AT("createdAt");

    private final String fieldName;

    SortField(String fieldName){
        this.fieldName = fieldName;
    }

    public static SortField fromString(String field){
        for (SortField sort : SortField.values()){
            if(sort.fieldName.equalsIgnoreCase(field)){
                return sort;
            }
        }
        log.warn("Invalid sort field: {}. Defaulting to CREATED_AT", field);
        return CREATED_AT;
    }


}
