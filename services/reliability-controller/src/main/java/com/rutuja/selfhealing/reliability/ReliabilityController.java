package com.rutuja.selfhealing.reliability;

import com.rutuja.selfhealing.reliability.model.FailureType;
import com.rutuja.selfhealing.reliability.model.Incident;
import com.rutuja.selfhealing.reliability.model.IncidentStatus;
import com.rutuja.selfhealing.reliability.orchestrator.RecoveryOrchestratorService;
import com.rutuja.selfhealing.reliability.service.FailureDetectionService;
import com.rutuja.selfhealing.reliability.service.IncidentService;
import com.rutuja.selfhealing.reliability.service.RemediationService;
import com.rutuja.selfhealing.reliability.service.VerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReliabilityController {

    private final FailureDetectionService failureDetectionService;
    private final IncidentService incidentService;
    private final RemediationService remediationService;
    private final VerificationService verificationService;
    private final RecoveryOrchestratorService recoveryOrchestratorService;
    private final RestClient paymentClient;

    public ReliabilityController(
            FailureDetectionService failureDetectionService,
            IncidentService incidentService,
            RemediationService remediationService,
            VerificationService verificationService,
            RecoveryOrchestratorService recoveryOrchestratorService,
            @Value("${payment-service.base-url}") String paymentServiceUrl) {

        this.failureDetectionService = failureDetectionService;
        this.incidentService = incidentService;
        this.remediationService = remediationService;
        this.verificationService = verificationService;
        this.recoveryOrchestratorService = recoveryOrchestratorService;

        this.paymentClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    @GetMapping("/reliability/status")
    public ResponseEntity<Map<String, Object>> getStatus() {

        boolean paymentHealthy =
                failureDetectionService.isPaymentHealthy();

        boolean orderHealthy =
                failureDetectionService.isOrderHealthy();

        return ResponseEntity.ok(Map.of(
                "controller", "UP",
                "paymentService", paymentHealthy ? "UP" : "DOWN",
                "orderService", orderHealthy ? "UP" : "DOWN",
                "incidentCount", incidentService.getAllIncidents().size()
        ));
    }

    @PostMapping("/failures/payment")
    public ResponseEntity<Map<String, Object>> injectPaymentFailure() {

        try {
            paymentClient.post()
                    .uri("/payments/fault")
                    .retrieve()
                    .toBodilessEntity();

            Incident incident = incidentService.createIncident(
                    "payment-service",
                    FailureType.SERVICE_UNAVAILABLE
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Payment failure injected",
                    "incidentId", incident.getId(),
                    "failureType", incident.getFailureType(),
                    "status", incident.getStatus(),
                    "createdAt", incident.getDetectedAt().toString()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                    "message", "Unable to inject payment failure",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/recovery/payment")
    public ResponseEntity<Map<String, Object>> recoverPayment() {

        Incident incident = incidentService.getLatestIncident();

        if (incident == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "No incident available for recovery"
            ));
        }

        incidentService.updateStatus(
                incident,
                IncidentStatus.REMEDIATING
        );

        boolean remediationSuccessful =
                remediationService.recoverPaymentService();

        if (!remediationSuccessful) {

            incidentService.updateStatus(
                    incident,
                    IncidentStatus.RECOVERY_FAILED
            );

            return ResponseEntity.status(503).body(Map.of(
                    "message", "Payment recovery action failed",
                    "status", "RECOVERY_FAILED",
                    "incidentId", incident.getId()
            ));
        }

        incidentService.updateStatus(
                incident,
                IncidentStatus.VERIFYING
        );

        return ResponseEntity.ok(Map.of(
                "message", "Payment recovery action executed",
                "action", "CLEAR_FAILURE",
                "incidentId", incident.getId(),
                "status", incident.getStatus()
        ));
    }

    @PostMapping("/recovery/payment/verify")
    public ResponseEntity<Map<String, Object>> verifyPaymentRecovery() {

        Incident incident = incidentService.getLatestIncident();

        if (incident == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "No incident available for verification"
            ));
        }

        Map<String, Object> verification =
                verificationService.verifyPaymentRecovery();

        boolean recovered =
                Boolean.TRUE.equals(verification.get("recovered"));

        if (recovered) {
            incidentService.markRecovered(incident);
        } else {
            incidentService.updateStatus(
                    incident,
                    IncidentStatus.RECOVERY_FAILED
            );
        }

        return ResponseEntity.ok(verification);
    }

    @PostMapping("/recovery/payment/auto")
    public ResponseEntity<Map<String, Object>> automaticPaymentRecovery() {

        Map<String, Object> result =
                recoveryOrchestratorService.recoverLatestPaymentIncident();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<Incident>> getIncidents() {

        return ResponseEntity.ok(
                incidentService.getAllIncidents()
        );
    }
}
