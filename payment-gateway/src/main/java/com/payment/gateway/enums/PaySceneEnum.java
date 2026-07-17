package com.payment.gateway.enums;

/**
 * Supported payment scenarios.
 */
public enum PaySceneEnum {

    NATIVE("NATIVE", "QR Code Payment"),
    H5("H5", "Mobile H5 Payment"),
    APP("APP", "In-App Payment");

    private final String code;
    private final String displayName;

    PaySceneEnum(String code, String displayName) {
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
