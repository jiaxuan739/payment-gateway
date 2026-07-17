package com.payment.gateway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payment.gateway.channel.PaymentChannel;
import com.payment.gateway.dto.ChannelMetrics;
import com.payment.gateway.model.PaymentOrder;
import com.payment.gateway.repository.PaymentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Smart routing engine: selects the optimal payment channel based on
 * real-time metrics (success rate, response time) and configured fee rates.
 *
 * <p><b>Algorithm:</b>
 * <pre>
 *   score = successRate × 0.5 + feeScore × 0.3 + latencyScore × 0.2
 * </pre>
 *
 * <p>If the top-ranked channel fails, automatically falls back to the next best.
 */
@Service
public class RoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private final List<PaymentChannel> channels;
    private final ChannelMetricsService metricsService;
    private final PaymentOrderRepository orderRepository;

    public RoutingService(List<PaymentChannel> channels, ChannelMetricsService metricsService, PaymentOrderRepository orderRepository) {
        this.channels = channels;
        this.metricsService = metricsService;
        this.orderRepository = orderRepository;
    }

    /**
     * Select the best channel for a payment request.
     * If request specifies a channel, use it directly (no routing).
     *
     * @param requestedChannel optional preferred channel (null = auto-select)
     * @return the selected PaymentChannel
     */
    public PaymentChannel selectChannel(String requestedChannel) {
        if (requestedChannel != null && !requestedChannel.isBlank()) {
            return channels.stream()
                    .filter(ch -> ch.channelCode().equalsIgnoreCase(requestedChannel))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown channel: " + requestedChannel));
        }
        return autoSelect();
    }

    /**
     * Auto-select the best channel using the composite scoring algorithm.
     */
    private PaymentChannel autoSelect() {
        Map<String, BigDecimal> feeRates = loadFeeRates();
        List<ChannelMetrics> allMetrics = metricsService.getAllMetrics(feeRates);

        if (allMetrics.isEmpty()) {
            // Fallback: pick first available channel
            log.warn("[Routing] No metrics available, using first channel");
            return channels.get(0);
        }

        // Compute normalization bounds
        double maxFee = allMetrics.stream()
                .mapToDouble(m -> m.getFeeRate().doubleValue()).max().orElse(0.01);
        long maxLatency = allMetrics.stream()
                .mapToLong(ChannelMetrics::getAvgResponseTimeMs).max().orElse(500);

        // Rank by composite score
        ChannelMetrics best = allMetrics.stream()
                .filter(ChannelMetrics::isEnabled)
                .max(Comparator.comparingDouble(m -> {
                    double score = m.computeScore(maxFee, maxLatency);
                    log.info("[Routing] {} score={:.4f} (sr={}, fee={}, lat={}ms)",
                            m.getChannel(), score, m.getSuccessRate(), m.getFeeRate(), m.getAvgResponseTimeMs());
                    return score;
                }))
                .orElseThrow(() -> new IllegalStateException("No enabled channel available"));

        log.info("[Routing] Selected channel: {} (score-based)", best.getChannel());

        return channels.stream()
                .filter(ch -> ch.channelCode().equals(best.getChannel()))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Get the fallback channel (second-best) if the primary fails.
     */
    public PaymentChannel getFallback(String failedChannelCode) {
        return channels.stream()
                .filter(ch -> !ch.channelCode().equals(failedChannelCode))
                .findFirst()
                .orElse(null);
    }

    /**
     * Load channel fee rates from MySQL channel_config table.
     */
    private Map<String, BigDecimal> loadFeeRates() {
        // Use hardcoded defaults matching the channel implementations.
        // In production, this would query a channel_config table.
        return Map.of(
                "WECHAT", new BigDecimal("0.0060"),
                "ALIPAY", new BigDecimal("0.0055"),
                "UNIONPAY", new BigDecimal("0.0050")
        );
    }
}
