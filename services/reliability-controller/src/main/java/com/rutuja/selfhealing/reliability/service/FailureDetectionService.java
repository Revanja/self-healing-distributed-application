package com.rutuja.selfhealing.reliability.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FailureDetectionService {

    private final RestClient paymentClient;
    private final RestClient orderClient;

    public FailureDetectionService(
            @Value("${payment-service.base-url}") String paymentServiceUrl,
            @Value("${order-service.base-url}") String orderServiceUrl) {

        this.paymentClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();

        this.orderClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
    }

    public boolean isPaymentHealthy() {
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

    public boolean isOrderHealthy() {
        try {
            orderClient.get()
                    .uri("/orders/health")
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean areAllServicesHealthy() {
        return isPaymentHealthy() && isOrderHealthy();
    }
}
