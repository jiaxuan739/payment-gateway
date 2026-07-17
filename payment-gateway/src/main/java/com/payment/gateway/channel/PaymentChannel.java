package com.payment.gateway.channel;

import com.payment.gateway.model.PaymentOrder;

import java.util.List;
import java.util.Map;

/**
 * Unified payment channel interface.
 * Each payment provider (WeChat Pay, Alipay, UnionPay) implements this.
 */
public interface PaymentChannel {

    /** Channel identifier matching PayChannelEnum.code */
    String channelCode();

    /**
     * Execute payment.
     * @return map with keys: tradeNo (channel transaction number), extraData (optional JSON)
     */
    Map<String, String> pay(PaymentOrder order);

    /** Query payment status from the channel */
    Map<String, String> query(PaymentOrder order);

    /** Submit refund request */
    Map<String, String> refund(PaymentOrder order);

    /**
     * Download yesterday's bill from the channel.
     * @param billDate yyyy-MM-dd
     * @return list of bill records, each containing: tradeNo, amount, status, settleTime
     */
    List<Map<String, String>> downloadBill(String billDate);
}
