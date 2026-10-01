package com.rutuja.selfhealing.reliability.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class VerificationService {

    private final RestClient paymentClient;
    private final RestClient orderClient;

    public VerificationService(
            @Value("${payment-service.base-url}") String paymentServiceUrl,
            @Value("${order-service.base-url}") String orderServiceUrl) {

        this.paymentClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();

        this.orderClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    public Map<String, Object> verifyPaymentRecovery() {

        boolean paymentHealthy = isPaymentHealthy();

        if (!paymentHealthy) {
            return Map.of(
                    "recovered", false,
                    "status", "RECOVERY_FAILED",
                    "reason", "Payment health check failed"
            );
        }

        boolean paymentOperationSuccessful = testPaymentOperation();

        if (!paymentOperationSuccessful) {
            return Map.of(
                    "recovered", false,
                    "status", "RECOVERY_FAILED",
                    "reason", "Payment health passed but real payment operation failed"
            );
        }

        boolean orderOperationSuccessful = testOrderOperation();

        if (!orderOperationSuccessful) {
            return Map.of(
                    "recovered", false,
                    "status", "RECOVERY_FAILED",
                    "reason", "Payment recovered but end-to-end order operation failed"
            );
        }

        return Map.of(
                "recovered", true,
                "status", "RECOVERED",
                "verification", "PASSED",
                "checks", List.of(
                        "payment health",
                        "payment operation",
                        "end-to-end order operation"
                )
        );
    }

    private boolean isPaymentHealthy() {
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

    private boolean testPaymentOperation() {
        try {
            paymentClient.post()
                    .uri("/payments/process")
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private boolean testOrderOperation() {
        try {
            orderClient.post()
                    .uri("/orders")
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
