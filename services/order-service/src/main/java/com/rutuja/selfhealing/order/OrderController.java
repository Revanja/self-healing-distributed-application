package com.rutuja.selfhealing.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final RestClient paymentClient;

    public OrderController(
            @Value("${payment-service.base-url}") String paymentServiceUrl) {

        this.paymentClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            paymentClient.get()
                    .uri("/payments/health")
                    .retrieve()
                    .toBodilessEntity();

            return ResponseEntity.ok(Map.of(
                    "service", "order-service",
                    "status", "UP",
                    "paymentDependency", "UP"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "service", "order-service",
                    "status", "DEGRADED",
                    "paymentDependency", "DOWN"
            ));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean paymentAvailable = isPaymentAvailable();

        return ResponseEntity.ok(Map.of(
                "service", "order-service",
                "status", paymentAvailable ? "READY" : "DEGRADED",
                "paymentDependency", paymentAvailable ? "UP" : "DOWN"
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder() {

        try {
            Map<?, ?> paymentResponse = paymentClient.post()
                    .uri("/payments/process")
                    .retrieve()
                    .body(Map.class);

            return ResponseEntity.ok(Map.of(
                    "orderId", "ORD-" + UUID.randomUUID(),
                    "status", "CREATED",
                    "payment", paymentResponse
            ));

        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "FAILED",
                    "reason", "Payment service unavailable"
            ));
        }
    }

    private boolean isPaymentAvailable() {
        try {
            paymentClient.get()
                    .uri("/payments/health")
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}