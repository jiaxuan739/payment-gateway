package com.payment.gateway.config;

import com.payment.gateway.service.ReconciliationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduled tasks configuration.
 * <p>
 * Reconciliation runs daily at 02:00 AM (Asia/Shanghai).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    private final ReconciliationService reconciliationService;

    public SchedulingConfig(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * Daily reconciliation job: pulls channel bills and matches against local orders.
     * Cron: 0 0 2 * * ? = every day at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    public void scheduledReconciliation() {
        log.info("[Scheduler] Triggering daily reconciliation...");
        try {
            reconciliationService.runReconciliation();
        } catch (Exception e) {
            log.error("[Scheduler] Reconciliation failed", e);
        }
    }
}
