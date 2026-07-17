package com.payment.gateway.model;

import com.baomidou.mybatisplus.annotation.*;
import com.payment.gateway.enums.OrderStatusEnum;
import com.payment.gateway.enums.PayChannelEnum;
import com.payment.gateway.enums.PaySceneEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Merchant order number */
    private String orderNo;

    /** Channel transaction number (returned by channel after payment) */
    private String tradeNo;

    /** Payment channel */
    private String channel;

    /** Payment scene */
    private String scene;

    /** Order amount in yuan */
    private BigDecimal amount;

    /** Order subject */
    private String subject;

    /** Order description */
    private String body;

    /** Order status */
    private String status;

    /** Channel fee rate applied */
    private BigDecimal channelFee;

    /** Channel response time in milliseconds */
    private Integer responseTimeMs;

    /** Additional data (JSON) */
    private String extraData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // -- Getters and Setters --

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getChannelFee() {
        return channelFee;
    }

    public void setChannelFee(BigDecimal channelFee) {
        this.channelFee = channelFee;
    }

    public Integer getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // -- Convenience methods --

    public boolean isPending() {
        return OrderStatusEnum.PENDING.getCode().equals(status);
    }

    public void markSuccess(String tradeNo, BigDecimal channelFee, int responseTimeMs) {
        this.status = OrderStatusEnum.SUCCESS.getCode();
        this.tradeNo = tradeNo;
        this.channelFee = channelFee;
        this.responseTimeMs = responseTimeMs;
    }

    public void markFailed() {
        this.status = OrderStatusEnum.FAILED.getCode();
    }
}
