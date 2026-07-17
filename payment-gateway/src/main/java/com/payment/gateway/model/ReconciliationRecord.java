package com.payment.gateway.model;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("reconciliation_record")
public class ReconciliationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Reconciliation batch number */
    private String batchNo;

    /** Payment channel */
    private String channel;

    /** Bill date (yyyy-MM-dd) */
    private String billDate;

    /** Local order number */
    private String localOrderNo;

    /** Channel transaction number */
    private String channelTradeNo;

    /** Amount from channel bill */
    private BigDecimal channelAmount;

    /** Amount in local order */
    private BigDecimal localAmount;

    /** Difference type: MATCH / LOCAL_ONLY / CHANNEL_ONLY / AMOUNT_MISMATCH */
    private String diffType;

    /** Difference detail description */
    private String diffDetail;

    /** Handle status: PENDING / HANDLED / IGNORED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // -- Getters and Setters --

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getLocalOrderNo() {
        return localOrderNo;
    }

    public void setLocalOrderNo(String localOrderNo) {
        this.localOrderNo = localOrderNo;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    public void setChannelTradeNo(String channelTradeNo) {
        this.channelTradeNo = channelTradeNo;
    }

    public BigDecimal getChannelAmount() {
        return channelAmount;
    }

    public void setChannelAmount(BigDecimal channelAmount) {
        this.channelAmount = channelAmount;
    }

    public BigDecimal getLocalAmount() {
        return localAmount;
    }

    public void setLocalAmount(BigDecimal localAmount) {
        this.localAmount = localAmount;
    }

    public String getDiffType() {
        return diffType;
    }

    public void setDiffType(String diffType) {
        this.diffType = diffType;
    }

    public String getDiffDetail() {
        return diffDetail;
    }

    public void setDiffDetail(String diffDetail) {
        this.diffDetail = diffDetail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
