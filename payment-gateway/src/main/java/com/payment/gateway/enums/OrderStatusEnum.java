package com.payment.gateway.enums;

/**
 * Payment order lifecycle states.
 */
public enum OrderStatusEnum {

    PENDING("PENDING", "Awaiting payment"),
    SUCCESS("SUCCESS", "Payment successful"),
    FAILED("FAILED", "Payment failed"),
    CLOSED("CLOSED", "Order closed"),
    REFUNDED("REFUNDED", "Order refunded");

    private final String code;
    private final String displayName;

    OrderStatusEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
