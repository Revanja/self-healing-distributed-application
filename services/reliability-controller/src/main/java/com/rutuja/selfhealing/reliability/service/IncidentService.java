package com.rutuja.selfhealing.reliability.service;

import com.rutuja.selfhealing.reliability.model.FailureType;
import com.rutuja.selfhealing.reliability.model.Incident;
import com.rutuja.selfhealing.reliability.model.IncidentStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IncidentService {

    private final List<Incident> incidents = new ArrayList<>();
    private final AtomicInteger incidentCounter = new AtomicInteger(0);

    public Incident createIncident(
            String service,
            FailureType failureType) {

        Incident incident = new Incident(
                incidentCounter.incrementAndGet(),
                service,
                failureType
        );

        incidents.add(incident);

        return incident;
    }

    public Incident getLatestIncident() {
        if (incidents.isEmpty()) {
            return null;
        }

        return incidents.get(incidents.size() - 1);
    }

    public List<Incident> getAllIncidents() {
        return new ArrayList<>(incidents);
    }

    public void updateStatus(
            Incident incident,
            IncidentStatus status) {

        incident.setStatus(status);
    }

    public void markRecovered(Incident incident) {
        incident.markRecovered();
    }
}
