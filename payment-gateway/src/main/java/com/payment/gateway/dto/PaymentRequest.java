package com.payment.gateway.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PaymentRequest {

    /** Merchant order number */
    @NotBlank(message = "orderNo is required")
    private String orderNo;

    /** Amount in yuan */
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be >= 0.01")
    private BigDecimal amount;

    /** Preferred channel (optional; auto-routed if blank) */
    private String channel;

    /** Payment scene: NATIVE / H5 / APP */
    @NotBlank(message = "scene is required")
    private String scene;

    /** Order subject */
    private String subject;

    /** Order description */
    private String body;

    // -- Getters and Setters --

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
