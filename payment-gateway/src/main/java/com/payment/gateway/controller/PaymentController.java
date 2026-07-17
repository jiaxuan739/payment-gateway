package com.payment.gateway.controller;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.PaymentResponse;
import com.payment.gateway.model.PaymentOrder;
import com.payment.gateway.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Unified payment REST API.
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/payment/create — create and execute a payment</li>
 *   <li>GET /api/payment/query/{orderNo} — query order status</li>
 *   <li>POST /api/payment/refund — submit a refund</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create a payment order and execute it through the optimal channel.
     * <p>
     * Example request:
     * <pre>
     * {
     *   "orderNo": "ORD20260717001",
     *   "amount": 100.00,
     *   "scene": "NATIVE",
     *   "subject": "Test Order",
     *   "body": "Payment for test goods"
     * }
     * </pre>
     */
    @PostMapping("/create")
    public PaymentResponse<Map<String, Object>> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            Map<String, Object> result = paymentService.createPayment(request);
            return PaymentResponse.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("[API] Invalid payment request: {}", e.getMessage());
            return PaymentResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("[API] Payment error", e);
            return PaymentResponse.fail(500, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Query a payment order by merchant order number.
     */
    @GetMapping("/query/{orderNo}")
    public PaymentResponse<PaymentOrder> queryPayment(@PathVariable String orderNo) {
        try {
            PaymentOrder order = paymentService.queryPayment(orderNo);
            return PaymentResponse.ok(order);
        } catch (IllegalArgumentException e) {
            return PaymentResponse.fail(404, e.getMessage());
        }
    }

    /**
     * Submit a refund for a completed order.
     * <p>
     * Example request:
     * <pre>
     * { "orderNo": "ORD20260717001" }
     * </pre>
     */
    @PostMapping("/refund")
    public PaymentResponse<Map<String, String>> refund(@RequestBody Map<String, String> body) {
        String orderNo = body.get("orderNo");
        if (orderNo == null || orderNo.isBlank()) {
            return PaymentResponse.fail(400, "orderNo is required");
        }
        try {
            Map<String, String> result = paymentService.refund(orderNo);
            return PaymentResponse.ok(result);
        } catch (IllegalArgumentException e) {
            return PaymentResponse.fail(404, e.getMessage());
        } catch (IllegalStateException e) {
            return PaymentResponse.fail(409, e.getMessage());
        }
    }
}
