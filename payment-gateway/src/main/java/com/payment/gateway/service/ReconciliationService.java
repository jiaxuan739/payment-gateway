package com.payment.gateway.service;

import com.payment.gateway.channel.PaymentChannel;
import com.payment.gateway.dto.ReconciliationResult;
import com.payment.gateway.model.PaymentOrder;
import com.payment.gateway.model.ReconciliationRecord;
import com.payment.gateway.repository.PaymentOrderRepository;
import com.payment.gateway.repository.ReconciliationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Async reconciliation system.
 * <p>
 * Scheduled daily at 2 AM: pulls bills from each channel,
 * matches against local orders by tradeNo + amount, and
 * records discrepancies. Sends alerts via RabbitMQ (simulating
 * DingTalk webhook notifications).
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);
    private static final DateTimeFormatter BILL_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final List<PaymentChannel> channels;
    private final PaymentOrderRepository orderRepository;
    private final ReconciliationRecordRepository reconciliationRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReconciliationService(List<PaymentChannel> channels, PaymentOrderRepository orderRepository, ReconciliationRecordRepository reconciliationRepository, RabbitTemplate rabbitTemplate) {
        this.channels = channels;
        this.orderRepository = orderRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Trigger a full reconciliation run for yesterday's data.
     * Called by @Scheduled or manually via controller.
     */
    @Transactional
    public ReconciliationResult runReconciliation() {
        String billDate = LocalDate.now().minusDays(1).format(BILL_DATE_FMT);
        return runReconciliation(billDate);
    }

    /**
     * Run reconciliation for a specific date.
     */
    @Transactional
    public ReconciliationResult runReconciliation(String billDate) {
        String batchNo = "REC" + System.currentTimeMillis();
        log.info("[Reconciliation] Starting batch={}, date={}", batchNo, billDate);

        ReconciliationResult summary = ReconciliationResult.builder()
                .batchNo(batchNo)
                .billDate(billDate)
                .build();

        int totalRecords = 0, matched = 0, localOnly = 0, channelOnly = 0, amountMismatch = 0;

        for (PaymentChannel channel : channels) {
            // Download channel bill
            List<Map<String, String>> channelBill = channel.downloadBill(billDate);
            // Load local orders for this channel + date
            List<PaymentOrder> localOrders = orderRepository.findSuccessOrdersByDate(channel.channelCode(), billDate);

            totalRecords += channelBill.size() + localOrders.size();

            // Build lookup maps
            Map<String, PaymentOrder> localByTradeNo = new HashMap<>();
            for (PaymentOrder o : localOrders) {
                if (o.getTradeNo() != null) {
                    localByTradeNo.put(o.getTradeNo(), o);
                }
            }

            Set<String> channelTradeNos = new HashSet<>();

            // Match channel bill → local
            for (Map<String, String> billRecord : channelBill) {
                String tradeNo = billRecord.get("tradeNo");
                BigDecimal channelAmount = new BigDecimal(billRecord.get("amount"));
                channelTradeNos.add(tradeNo);

                PaymentOrder local = localByTradeNo.get(tradeNo);
                if (local == null) {
                    channelOnly++;
                    saveDiff(batchNo, channel.channelCode(), billDate, null, tradeNo,
                            channelAmount, null, "CHANNEL_ONLY",
                            "Transaction exists in channel bill but not in local system");
                } else if (channelAmount.compareTo(local.getAmount()) != 0) {
                    amountMismatch++;
                    saveDiff(batchNo, channel.channelCode(), billDate, local.getOrderNo(), tradeNo,
                            channelAmount, local.getAmount(), "AMOUNT_MISMATCH",
                            String.format("Channel=%.2f vs Local=%.2f", channelAmount, local.getAmount()));
                } else {
                    matched++;
                    saveDiff(batchNo, channel.channelCode(), billDate, local.getOrderNo(), tradeNo,
                            channelAmount, local.getAmount(), "MATCH", null);
                }
            }

            // Find local orders not in channel bill
            for (PaymentOrder local : localOrders) {
                if (local.getTradeNo() != null && !channelTradeNos.contains(local.getTradeNo())) {
                    localOnly++;
                    saveDiff(batchNo, channel.channelCode(), billDate, local.getOrderNo(), local.getTradeNo(),
                            null, local.getAmount(), "LOCAL_ONLY",
                            "Transaction exists locally but not in channel bill");
                }
            }
        }

        summary.setTotalRecords(totalRecords);
        summary.setMatched(matched);
        summary.setLocalOnly(localOnly);
        summary.setChannelOnly(channelOnly);
        summary.setAmountMismatch(amountMismatch);

        // Send alert if discrepancies found
        if (summary.hasErrors()) {
            String alertMsg = summary.toAlertMessage();
            log.warn("[Reconciliation] Discrepancies detected: {}", alertMsg);
            rabbitTemplate.convertAndSend("payment.alert", "reconciliation.error", alertMsg);
        } else {
            log.info("[Reconciliation] All clear — no discrepancies");
        }

        log.info("[Reconciliation] Complete: batch={}, matched={}, localOnly={}, channelOnly={}, amountMismatch={}",
                batchNo, matched, localOnly, channelOnly, amountMismatch);
        return summary;
    }

    /**
     * Get recent reconciliation errors (for dashboard/alerting).
     */
    public List<ReconciliationRecord> getRecentErrors(int limit) {
        return reconciliationRepository.findRecentErrors(limit);
    }

    private void saveDiff(String batchNo, String channel, String billDate,
                          String localOrderNo, String tradeNo,
                          BigDecimal channelAmount, BigDecimal localAmount,
                          String diffType, String diffDetail) {
        ReconciliationRecord record = new ReconciliationRecord();
        record.setBatchNo(batchNo);
        record.setChannel(channel);
        record.setBillDate(billDate);
        record.setLocalOrderNo(localOrderNo);
        record.setChannelTradeNo(tradeNo);
        record.setChannelAmount(channelAmount);
        record.setLocalAmount(localAmount);
        record.setDiffType(diffType);
        record.setDiffDetail(diffDetail);
        record.setStatus("PENDING");
        reconciliationRepository.insert(record);
    }
}
