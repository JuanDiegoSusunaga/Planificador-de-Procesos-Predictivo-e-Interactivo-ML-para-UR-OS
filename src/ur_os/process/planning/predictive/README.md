# Predictive Planning Module (Phase 3)

This package isolates the new Machine Learning-based scheduling algorithms from the classic academic algorithms (like FCFS, Round Robin).

## Purpose
To implement the `ML_Scheduler` that utilizes the exported Machine Learning model (e.g., a Decision Tree) to dynamically adjust scheduling priorities based on real-time process telemetry and the global "User Intent" profile.

## Rules
- Schedulers in this package must implement the standard `Scheduler` interface or base class from `ur_os.process.planning`.
- The inference logic inside these schedulers must be extremely fast (millisecond execution) to avoid blocking context switches.
