## Self-Healing Distributed Application

## Overview

## Problem Statement

## Core Idea

## Architecture

## Current Recovery Flow

## Services

### Order Service
### Payment Service
### Reliability Controller

## Failure Injection

## Recovery Verification

## API Endpoints

## Tech Stack

## Project Structure

## How to Run Locally

## Testing

## Current Implementation Status

## Limitations

## Roadmap

## Engineering Decisions

## Future Experiments

## Current Implementation Status

### Implemented

- Distributed Order Service and Payment Service
- Controlled Payment Service failure injection
- Failure detection
- Incident creation and lifecycle tracking
- Remediation action execution
- Application-level recovery verification
- End-to-end order verification after remediation
- Recovery orchestration
- Recovery states:
  - DETECTED
  - REMEDIATING
  - VERIFYING
  - RECOVERED
  - RECOVERY_FAILED
  - ESCALATED
- Automated tests for the reliability controller

### Current Limitation

The current remediation mechanism clears a controlled injected
failure rather than restarting a real process or container.

The recovery endpoint is also currently triggered explicitly.
Continuous autonomous failure detection and production-style
remediation are part of the next development stages.
