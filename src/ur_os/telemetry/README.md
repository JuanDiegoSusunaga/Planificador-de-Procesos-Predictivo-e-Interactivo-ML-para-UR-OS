# Telemetry Module (Phase 1)

This package contains the logic for gathering performance and behavior metrics from processes during runtime.

## Purpose
To collect $O(1)$ complexity telemetry without degrading the performance of the UR-OS simulator. 
This data includes:
- CPU Burst occurrences and durations.
- I/O Block frequencies.
- User Intent Profile context (e.g., Development, Office, Multimedia).

This data is essential for generating the CSV dataset that will be used in the Machine Learning Phase (Phase 2).
