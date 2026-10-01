# Architecture

```text
                         React Dashboard
                               |
                               v
                    Reliability Controller
                       /               \
                      v                 v
              Order Service -----> Payment Service
                                      |
                               Fault Injection
```

Recovery:

```text
Failure
  -> Detection
  -> Remediation
  -> Independent Verification
  -> RECOVERED / RECOVERY_FAILED
```

The first remediation strategy clears a deliberately injected application fault. A later Docker adapter will support actual container restart without changing the verification layer.
