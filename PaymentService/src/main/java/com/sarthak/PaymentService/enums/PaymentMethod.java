package com.sarthak.PaymentService.enums;

public enum PaymentMethod {
    CREDIT_CARD,
    NET_BANKING,
    UPI,
    WALLET,
    CASH;

    public static PaymentMethod fromString(String method) {
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equalsIgnoreCase(method)) {
                return pm;
            }
        }
        return null;
    }
}
