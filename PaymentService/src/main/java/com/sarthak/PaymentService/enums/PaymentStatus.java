package com.sarthak.PaymentService.enums;

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    DECLINED;

    public static PaymentStatus fromString(String status){
        for(PaymentStatus paymentStatus : PaymentStatus.values()){
            if(paymentStatus.name().equalsIgnoreCase(status)){
                return paymentStatus;
            }
        }
        return null;
    }
}
