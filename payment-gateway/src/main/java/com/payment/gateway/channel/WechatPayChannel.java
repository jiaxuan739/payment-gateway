package com.payment.gateway.channel;

import com.payment.gateway.enums.PayChannelEnum;
import com.payment.gateway.model.PaymentOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * WeChat Pay channel (simulated).
 * <p>
 * Characteristics: higher success rate (~98%), moderate latency (~300ms), fee 0.60%.
 */
@Component
public class WechatPayChannel implements PaymentChannel {

    private static final Logger log = LoggerFactory.getLogger(WechatPayChannel.class);

    private static final double SUCCESS_RATE = 0.98;
    private static final int BASE_DELAY_MS = 200;
    private static final int DELAY_JITTER_MS = 200;
    private final Random random = new Random();

    @Override
    public String channelCode() {
        return PayChannelEnum.WECHAT.getCode();
    }

    @Override
    public Map<String, String> pay(PaymentOrder order) {
        log.info("[WeChat Pay] Processing payment: orderNo={}, amount={}", order.getOrderNo(), order.getAmount());
        simulateDelay();

        Map<String, String> result = new HashMap<>();
        if (random.nextDouble() < SUCCESS_RATE) {
            String tradeNo = "WX" + System.currentTimeMillis() + random.nextInt(10000);
            result.put("tradeNo", tradeNo);
            result.put("status", "SUCCESS");
            log.info("[WeChat Pay] Payment success: tradeNo={}", tradeNo);
        } else {
            result.put("tradeNo", null);
            result.put("status", "FAILED");
            result.put("errorMsg", "Channel returned failure");
            log.warn("[WeChat Pay] Payment failed: orderNo={}", order.getOrderNo());
        }
        return result;
    }

    @Override
    public Map<String, String> query(PaymentOrder order) {
        log.info("[WeChat Pay] Querying order: tradeNo={}", order.getTradeNo());
        Map<String, String> result = new HashMap<>();
        result.put("tradeNo", order.getTradeNo());
        result.put("status", order.getStatus());
        return result;
    }

    @Override
    public Map<String, String> refund(PaymentOrder order) {
        log.info("[WeChat Pay] Processing refund: tradeNo={}, amount={}", order.getTradeNo(), order.getAmount());
        simulateDelay();
        Map<String, String> result = new HashMap<>();
        result.put("refundNo", "RFWX" + System.currentTimeMillis());
        result.put("status", "SUCCESS");
        return result;
    }

    @Override
    public List<Map<String, String>> downloadBill(String billDate) {
        log.info("[WeChat Pay] Downloading bill for {}", billDate);
        return generateMockBill(billDate, "WX");
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
            record.put("settleTime", billDate + " " + String.format("%02d:%02d:00", 8 + i, random.nextInt(60)));
            bill.add(record);
        }
        return bill;
    }
}
