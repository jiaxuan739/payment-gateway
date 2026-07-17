package com.payment.gateway.channel;

import com.payment.gateway.enums.PayChannelEnum;
import com.payment.gateway.model.PaymentOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * UnionPay channel (simulated).
 * <p>
 * Characteristics: lowest fee (0.50%), moderate success rate (~96%), higher latency (~400ms).
 */
@Component
public class UnionPayChannel implements PaymentChannel {

    private static final Logger log = LoggerFactory.getLogger(UnionPayChannel.class);

    private static final double SUCCESS_RATE = 0.96;
    private static final int BASE_DELAY_MS = 250;
    private static final int DELAY_JITTER_MS = 300;
    private final Random random = new Random();

    @Override
    public String channelCode() {
        return PayChannelEnum.UNIONPAY.getCode();
    }

    @Override
    public Map<String, String> pay(PaymentOrder order) {
        log.info("[UnionPay] Processing payment: orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
        simulateDelay();

        Map<String, String> result = new HashMap<>();
        if (random.nextDouble() < SUCCESS_RATE) {
            String tradeNo = "UP" + System.currentTimeMillis() + random.nextInt(10000);
            result.put("tradeNo", tradeNo);
            result.put("status", "SUCCESS");
            log.info("[UnionPay] Payment success: tradeNo={}", tradeNo);
        } else {
            result.put("tradeNo", null);
            result.put("status", "FAILED");
            result.put("errorMsg", "Channel returned failure");
            log.warn("[UnionPay] Payment failed: orderNo={}", order.getOrderNo());
        }
        return result;
    }

    @Override
    public Map<String, String> query(PaymentOrder order) {
        log.info("[UnionPay] Querying order: tradeNo={}", order.getTradeNo());
        Map<String, String> result = new HashMap<>();
        result.put("tradeNo", order.getTradeNo());
        result.put("status", order.getStatus());
        return result;
    }

    @Override
    public Map<String, String> refund(PaymentOrder order) {
        log.info("[UnionPay] Processing refund: tradeNo={}, amount={}", order.getTradeNo(), order.getAmount());
        simulateDelay();
        Map<String, String> result = new HashMap<>();
        result.put("refundNo", "RFUP" + System.currentTimeMillis());
        result.put("status", "SUCCESS");
        return result;
    }

    @Override
    public List<Map<String, String>> downloadBill(String billDate) {
        log.info("[UnionPay] Downloading bill for {}", billDate);
        return generateMockBill(billDate, "UP");
    }

    // -- helpers --

    private void simulateDelay() {
        try {
            Thread.sleep(BASE_DELAY_MS + random.nextInt(DELAY_JITTER_MS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<Map<String, String>> generateMockBill(String billDate, String prefix) {
        List<Map<String, String>> bill = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, String> record = new HashMap<>();
            record.put("tradeNo", prefix + "BILL" + billDate.replace("-", "") + String.format("%04d", i));
            record.put("amount", String.format("%.2f", 10.0 + random.nextDouble() * 990));
            record.put("status", "SUCCESS");
            record.put("settleTime", billDate + " " + String.format("%02d:%02d:00", 12 + i, random.nextInt(60)));
            bill.add(record);
        }
        return bill;
    }
}
