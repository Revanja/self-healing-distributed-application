package com.rutuja.selfhealing.payment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final AtomicBoolean failureInjected = new AtomicBoolean(false);

    @GetMapping("/health")
    public Map<String, Object> health() {
        if (failureInjected.get()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Injected payment failure");
        }
        return Map.of("service", "payment-service", "status", "healthy",
                "timestamp", Instant.now().toString());
    }

    @GetMapping
    public Map<String, Object> status() {
        return Map.of("service", "payment-service",
                "status", failureInjected.get() ? "degraded" : "healthy",
                "failureInjected", failureInjected.get());
    }

    @PostMapping("/process")
    public Map<String, Object> process() {
        if (failureInjected.get()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment processing unavailable");
        }
        return Map.of("paymentStatus", "APPROVED",
                "transactionId", "TX-" + System.currentTimeMillis(),
                "timestamp", Instant.now().toString());
    }

    @PostMapping("/fault")
    public Map<String, Object> injectFault() {
        failureInjected.set(true);
        return Map.of("fault", "PAYMENT_UNAVAILABLE", "enabled", true);
    }

    @DeleteMapping("/fault")
    public Map<String, Object> clearFault() {
        failureInjected.set(false);
        return Map.of("fault", "PAYMENT_UNAVAILABLE", "enabled", false);
    }
}
