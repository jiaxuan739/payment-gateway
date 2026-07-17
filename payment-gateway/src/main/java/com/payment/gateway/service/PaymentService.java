package com.payment.gateway.service;

import com.payment.gateway.channel.PaymentChannel;
import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.enums.OrderStatusEnum;
import com.payment.gateway.model.PaymentOrder;
import com.payment.gateway.repository.PaymentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final RoutingService routingService;
    private final ChannelMetricsService metricsService;
    private final PaymentOrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentService(RoutingService routingService, ChannelMetricsService metricsService, PaymentOrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.routingService = routingService;
        this.metricsService = metricsService;
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Create and execute a payment.
     * <ol>
     *   <li>Create order in DB (PENDING)</li>
     *   <li>Route to best channel (or use requested channel)</li>
     *   <li>Call channel.pay()</li>
     *   <li>Update order status + record metrics</li>
     *   <li>If failed, auto-retry with fallback channel</li>
     * </ol>
     */
    @Transactional
    public Map<String, Object> createPayment(PaymentRequest request) {
        // 1. Persist order
        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(request.getOrderNo());
        order.setAmount(request.getAmount());
        order.setScene(request.getScene());
        order.setSubject(request.getSubject());
        order.setBody(request.getBody());
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        orderRepository.insert(order);
        log.info("[Payment] Order created: orderNo={}, amount={}", request.getOrderNo(), request.getAmount());

        // 2. Select channel (smart routing or user-specified)
        PaymentChannel channel = routingService.selectChannel(request.getChannel());
        order.setChannel(channel.channelCode());

        // 3. Execute payment via selected channel
        long startTime = System.currentTimeMillis();
        Map<String, String> payResult = channel.pay(order);
        long elapsed = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new LinkedHashMap<>();

        if ("SUCCESS".equals(payResult.get("status"))) {
            // Success
            String tradeNo = payResult.get("tradeNo");
            BigDecimal feeRate = getFeeRate(channel.channelCode());
            order.markSuccess(tradeNo, feeRate, (int) elapsed);
            orderRepository.updateById(order);

            metricsService.recordPayment(channel.channelCode(), true, elapsed, feeRate);
            rabbitTemplate.convertAndSend("payment.notify", "payment.success", order.getOrderNo());

            response.put("orderNo", order.getOrderNo());
            response.put("tradeNo", tradeNo);
            response.put("channel", channel.channelCode());
            response.put("amount", order.getAmount());
            response.put("status", "SUCCESS");
            response.put("responseTimeMs", elapsed);
            log.info("[Payment] Success: orderNo={}, channel={}, elapsed={}ms", order.getOrderNo(), channel.channelCode(), elapsed);
        } else {
            // Failure — try fallback
            log.warn("[Payment] Primary channel {} failed for orderNo={}, trying fallback",
                    channel.channelCode(), order.getOrderNo());
            metricsService.recordPayment(channel.channelCode(), false, (int) elapsed, getFeeRate(channel.channelCode()));

            PaymentChannel fallback = routingService.getFallback(channel.channelCode());
            if (fallback != null) {
                long fbStart = System.currentTimeMillis();
                Map<String, String> fbResult = fallback.pay(order);
                long fbElapsed = System.currentTimeMillis() - fbStart;

                if ("SUCCESS".equals(fbResult.get("status"))) {
                    String tradeNo = fbResult.get("tradeNo");
                    BigDecimal feeRate = getFeeRate(fallback.channelCode());
                    order.setChannel(fallback.channelCode());
                    order.markSuccess(tradeNo, feeRate, (int) fbElapsed);
                    orderRepository.updateById(order);

                    metricsService.recordPayment(fallback.channelCode(), true, (int) fbElapsed, feeRate);
                    response.put("orderNo", order.getOrderNo());
                    response.put("tradeNo", tradeNo);
                    response.put("channel", fallback.channelCode());
                    response.put("amount", order.getAmount());
                    response.put("status", "SUCCESS");
                    response.put("responseTimeMs", fbElapsed);
                    response.put("fallback", true);
                    log.info("[Payment] Fallback success via {}: orderNo={}", fallback.channelCode(), order.getOrderNo());
                    return response;
                }
                metricsService.recordPayment(fallback.channelCode(), false, (int) fbElapsed, getFeeRate(fallback.channelCode()));
            }

            // Both failed
            order.markFailed();
            orderRepository.updateById(order);
            rabbitTemplate.convertAndSend("payment.notify", "payment.failed", order.getOrderNo());

            response.put("orderNo", order.getOrderNo());
            response.put("status", "FAILED");
            response.put("errorMsg", payResult.getOrDefault("errorMsg", "Payment failed"));
            log.error("[Payment] All channels failed for orderNo={}", order.getOrderNo());
        }

        return response;
    }

    /**
     * Query payment order status.
     */
    public PaymentOrder queryPayment(String orderNo) {
        PaymentOrder order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderNo);
        }
        return order;
    }

    /**
     * Process refund for a completed order.
     */
    @Transactional
    public Map<String, String> refund(String orderNo) {
        PaymentOrder order = orderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderNo);
        }
        if (!OrderStatusEnum.SUCCESS.getCode().equals(order.getStatus())) {
            throw new IllegalStateException("Only successful orders can be refunded. Current status: " + order.getStatus());
        }

        PaymentChannel channel = routingService.selectChannel(order.getChannel());
        Map<String, String> refundResult = channel.refund(order);

        order.setStatus(OrderStatusEnum.REFUNDED.getCode());
        orderRepository.updateById(order);

        log.info("[Payment] Refund processed: orderNo={}, refundNo={}", orderNo, refundResult.get("refundNo"));
        return refundResult;
    }

    private BigDecimal getFeeRate(String channelCode) {
        return switch (channelCode) {
            case "WECHAT" -> new BigDecimal("0.0060");
            case "ALIPAY" -> new BigDecimal("0.0055");
            case "UNIONPAY" -> new BigDecimal("0.0050");
            default -> BigDecimal.ZERO;
        };
    }
}
