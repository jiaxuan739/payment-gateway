package com.payment.gateway.dto;

/**
 * Summary of a reconciliation run.
 */
public class ReconciliationResult {

    /** Batch number for this run */
    private String batchNo;

    /** Bill date */
    private String billDate;

    /** Total records processed */
    private int totalRecords;

    /** Matched successfully */
    private int matched;

    /** Only in local system (missing from channel bill) */
    private int localOnly;

    /** Only in channel bill (missing from local system) */
    private int channelOnly;

    /** Amount mismatch */
    private int amountMismatch;

    public ReconciliationResult() {
    }

    public ReconciliationResult(String batchNo, String billDate, int totalRecords, int matched, int localOnly, int channelOnly, int amountMismatch) {
        this.batchNo = batchNo;
        this.billDate = billDate;
        this.totalRecords = totalRecords;
        this.matched = matched;
        this.localOnly = localOnly;
        this.channelOnly = channelOnly;
        this.amountMismatch = amountMismatch;
    }

    // -- Getters and Setters --

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getMatched() {
        return matched;
    }

    public void setMatched(int matched) {
        this.matched = matched;
    }

    public int getLocalOnly() {
        return localOnly;
    }

    public void setLocalOnly(int localOnly) {
        this.localOnly = localOnly;
    }

    public int getChannelOnly() {
        return channelOnly;
    }

    public void setChannelOnly(int channelOnly) {
        this.channelOnly = channelOnly;
    }

    public int getAmountMismatch() {
        return amountMismatch;
    }

    public void setAmountMismatch(int amountMismatch) {
        this.amountMismatch = amountMismatch;
    }

    // -- Builder --

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String batchNo;
        private String billDate;
        private int totalRecords;
        private int matched;
        private int localOnly;
        private int channelOnly;
        private int amountMismatch;

        public Builder batchNo(String batchNo) {
            this.batchNo = batchNo;
            return this;
        }

        public Builder billDate(String billDate) {
            this.billDate = billDate;
            return this;
        }

        public Builder totalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
            return this;
        }

        public Builder matched(int matched) {
            this.matched = matched;
            return this;
        }

        public Builder localOnly(int localOnly) {
            this.localOnly = localOnly;
            return this;
        }

        public Builder channelOnly(int channelOnly) {
            this.channelOnly = channelOnly;
            return this;
        }

        public Builder amountMismatch(int amountMismatch) {
            this.amountMismatch = amountMismatch;
            return this;
        }

        public ReconciliationResult build() {
            return new ReconciliationResult(batchNo, billDate, totalRecords, matched, localOnly, channelOnly, amountMismatch);
        }
    }

    public boolean hasErrors() {
        return localOnly > 0 || channelOnly > 0 || amountMismatch > 0;
    }

    public String toAlertMessage() {
        if (!hasErrors()) return "All records matched.";
        return String.format(
                "[Payment Gateway Alert] Reconciliation: %d matched, %d local-only, %d channel-only, %d amount-mismatch. Batch: %s, Date: %s",
                matched, localOnly, channelOnly, amountMismatch, batchNo, billDate);
    }
}
