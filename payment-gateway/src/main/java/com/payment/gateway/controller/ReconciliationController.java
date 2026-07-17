package com.payment.gateway.controller;

import com.payment.gateway.dto.PaymentResponse;
import com.payment.gateway.dto.ReconciliationResult;
import com.payment.gateway.model.ReconciliationRecord;
import com.payment.gateway.service.ReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reconciliation management endpoints.
 */
@RestController
@RequestMapping("/api/reconciliation")
public class ReconciliationController {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationController.class);

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * Manually trigger reconciliation for yesterday.
     */
    @PostMapping("/trigger")
    public PaymentResponse<ReconciliationResult> triggerReconciliation() {
        try {
            ReconciliationResult result = reconciliationService.runReconciliation();
            log.info("[API] Manual reconciliation triggered: batch={}", result.getBatchNo());
            return PaymentResponse.ok(result);
        } catch (Exception e) {
            log.error("[API] Reconciliation error", e);
            return PaymentResponse.fail(500, "Reconciliation failed: " + e.getMessage());
        }
    }

    /**
     * Get recent reconciliation errors for dashboard.
     */
    @GetMapping("/errors")
    public PaymentResponse<List<ReconciliationRecord>> getErrors(
            @RequestParam(defaultValue = "20") int limit) {
        List<ReconciliationRecord> errors = reconciliationService.getRecentErrors(limit);
        return PaymentResponse.ok(errors);
    }
}
