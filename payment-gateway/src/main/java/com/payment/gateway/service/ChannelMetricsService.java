package com.payment.gateway.service;

import com.payment.gateway.dto.ChannelMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Manages real-time channel performance metrics using Redis.
 * Each channel has a rolling window of recent payment results for
 * computing success rate and average response time.
 */
@Service
public class ChannelMetricsService {

    private static final Logger log = LoggerFactory.getLogger(ChannelMetricsService.class);
    private static final String METRICS_PREFIX = "channel:metrics:";
    private static final long METRICS_TTL_MINUTES = 30;

    private final RedisTemplate<String, Object> redisTemplate;

    public ChannelMetricsService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Record a payment result and update rolling metrics.
     */
    public void recordPayment(String channel, boolean success, long responseTimeMs, BigDecimal feeRate) {
        String key = METRICS_PREFIX + channel;
        ChannelMetrics current = getMetrics(channel, feeRate);

        // Exponential moving average update: new = 0.8 * old + 0.2 * sample
        double newSuccessRate = current.getSuccessRate() * 0.8 + (success ? 1.0 : 0.0) * 0.2;
        long newAvgLatency = (long) (current.getAvgResponseTimeMs() * 0.8 + responseTimeMs * 0.2);

        ChannelMetrics updated = ChannelMetrics.builder()
                .channel(channel)
                .successRate(round2(newSuccessRate))
                .avgResponseTimeMs(newAvgLatency)
                .feeRate(feeRate)
                .enabled(true)
                .build();

        redisTemplate.opsForValue().set(key, updated, METRICS_TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("[Metrics] Updated {}: successRate={}, avgLatency={}ms", channel, updated.getSuccessRate(), updated.getAvgResponseTimeMs());
    }

    /**
     * Get current metrics for a channel. Falls back to defaults if no data yet.
     */
    public ChannelMetrics getMetrics(String channel, BigDecimal feeRate) {
        String key = METRICS_PREFIX + channel;
        ChannelMetrics cached = (ChannelMetrics) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        // Defaults for a new channel (optimistic prior)
        return ChannelMetrics.builder()
                .channel(channel)
                .successRate(1.0)
                .avgResponseTimeMs(200L)
                .feeRate(feeRate)
                .enabled(true)
                .build();
    }

    /**
     * Get metrics for all known channels.
     */
    public List<ChannelMetrics> getAllMetrics(Map<String, BigDecimal> channelFeeRates) {
        List<ChannelMetrics> metrics = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : channelFeeRates.entrySet()) {
            metrics.add(getMetrics(entry.getKey(), entry.getValue()));
        }
        return metrics;
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
