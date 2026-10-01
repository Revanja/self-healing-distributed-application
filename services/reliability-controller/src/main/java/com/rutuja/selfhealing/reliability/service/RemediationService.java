package com.rutuja.selfhealing.reliability.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RemediationService {

    private final RestClient paymentClient;

    public RemediationService(
            @Value("${payment-service.base-url}") String paymentServiceUrl) {

        this.paymentClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    public boolean recoverPaymentService() {

        try {
            paymentClient.delete()
                    .uri("/payments/fault")
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
