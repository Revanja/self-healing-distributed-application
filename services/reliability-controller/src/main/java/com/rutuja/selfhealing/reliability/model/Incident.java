package com.rutuja.selfhealing.reliability.model;

import java.time.Instant;

public class Incident {

    private final int id;
    private final String service;
    private final FailureType failureType;
    private IncidentStatus status;
    private final Instant detectedAt;
    private Instant recoveredAt;

    public Incident(
            int id,
            String service,
            FailureType failureType) {

        this.id = id;
        this.service = service;
        this.failureType = failureType;
        this.status = IncidentStatus.DETECTED;
        this.detectedAt = Instant.now();
    }

    public int getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public FailureType getFailureType() {
        return failureType;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getRecoveredAt() {
        return recoveredAt;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public void markRecovered() {
        this.status = IncidentStatus.RECOVERED;
        this.recoveredAt = Instant.now();
    }
}
