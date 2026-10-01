package com.rutuja.selfhealing.reliability.orchestrator;

import com.rutuja.selfhealing.reliability.model.Incident;
import com.rutuja.selfhealing.reliability.model.IncidentStatus;
import com.rutuja.selfhealing.reliability.service.FailureDetectionService;
import com.rutuja.selfhealing.reliability.service.IncidentService;
import com.rutuja.selfhealing.reliability.service.RemediationService;
import com.rutuja.selfhealing.reliability.service.VerificationService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RecoveryOrchestratorService {

    private final FailureDetectionService failureDetectionService;
    private final IncidentService incidentService;
    private final RemediationService remediationService;
    private final VerificationService verificationService;

    public RecoveryOrchestratorService(
            FailureDetectionService failureDetectionService,
            IncidentService incidentService,
            RemediationService remediationService,
            VerificationService verificationService) {

        this.failureDetectionService = failureDetectionService;
        this.incidentService = incidentService;
        this.remediationService = remediationService;
        this.verificationService = verificationService;
    }

    public Map<String, Object> recoverLatestPaymentIncident() {

        Incident incident = incidentService.getLatestIncident();

        if (incident == null) {
            return Map.of(
                    "recovered", false,
                    "status", "NO_INCIDENT",
                    "message", "No incident available for recovery"
            );
        }

        boolean paymentFailed =
                !failureDetectionService.isPaymentHealthy();

        if (!paymentFailed) {
            return Map.of(
                    "recovered", false,
                    "status", "NO_FAILURE_DETECTED",
                    "incidentId", incident.getId(),
                    "message", "Payment service is already healthy"
            );
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

            return Map.of(
                    "recovered", false,
                    "status", "RECOVERY_FAILED",
                    "incidentId", incident.getId(),
                    "stage", "REMEDIATION"
            );
        }

        incidentService.updateStatus(
                incident,
                IncidentStatus.VERIFYING
        );

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

        return Map.of(
                "incidentId", incident.getId(),
                "recovery", verification,
                "finalStatus", incident.getStatus()
        );
    }
}
