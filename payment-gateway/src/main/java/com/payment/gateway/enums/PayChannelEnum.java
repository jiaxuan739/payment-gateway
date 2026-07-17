package com.payment.gateway.enums;

/**
 * Supported payment channels.
 */
public enum PayChannelEnum {

    WECHAT("WECHAT", "WeChat Pay"),
    ALIPAY("ALIPAY", "Alipay"),
    UNIONPAY("UNIONPAY", "UnionPay");

    private final String code;
    private final String displayName;

    PayChannelEnum(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PayChannelEnum fromCode(String code) {
        for (PayChannelEnum ch : values()) {
            if (ch.code.equalsIgnoreCase(code)) {
                return ch;
            }
        }
        throw new IllegalArgumentException("Unknown channel: " + code);
    }
}
