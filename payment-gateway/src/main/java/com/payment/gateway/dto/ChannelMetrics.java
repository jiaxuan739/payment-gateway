package com.payment.gateway.dto;

import java.math.BigDecimal;

/**
 * Real-time channel performance metrics (cached in Redis).
 */
public class ChannelMetrics {

    /** Channel code */
    private String channel;

    /** Rolling success rate (0.0 - 1.0) */
    private double successRate;

    /** Average response time in ms over recent window */
    private long avgResponseTimeMs;

    /** Configured fee rate (e.g. 0.0060 = 0.6%) */
    private BigDecimal feeRate;

    /** Whether the channel is enabled */
    private boolean enabled;

    public ChannelMetrics() {
    }

    public ChannelMetrics(String channel, double successRate, long avgResponseTimeMs, BigDecimal feeRate, boolean enabled) {
        this.channel = channel;
        this.successRate = successRate;
        this.avgResponseTimeMs = avgResponseTimeMs;
        this.feeRate = feeRate;
        this.enabled = enabled;
    }

    // -- Getters and Setters --

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public long getAvgResponseTimeMs() {
        return avgResponseTimeMs;
    }

    public void setAvgResponseTimeMs(long avgResponseTimeMs) {
        this.avgResponseTimeMs = avgResponseTimeMs;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // -- Builder --

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String channel;
        private double successRate;
        private long avgResponseTimeMs;
        private BigDecimal feeRate;
        private boolean enabled;

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder successRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public Builder avgResponseTimeMs(long avgResponseTimeMs) {
            this.avgResponseTimeMs = avgResponseTimeMs;
            return this;
        }

        public Builder feeRate(BigDecimal feeRate) {
            this.feeRate = feeRate;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ChannelMetrics build() {
            return new ChannelMetrics(channel, successRate, avgResponseTimeMs, feeRate, enabled);
        }
    }

    /**
     * Composite score for routing decisions.
     * Higher is better.
     * <p>
     * Formula: successRate * 0.5 + (1 - normalizedFee) * 0.3 + (1 - normalizedLatency) * 0.2
     */
    public double computeScore(double maxFee, long maxLatency) {
        double feeScore = maxFee > 0 ? 1.0 - (feeRate.doubleValue() / maxFee) : 0.5;
        double latencyScore = maxLatency > 0 ? 1.0 - ((double) avgResponseTimeMs / maxLatency) : 0.5;
        return successRate * 0.5 + feeScore * 0.3 + latencyScore * 0.2;
    }
}
