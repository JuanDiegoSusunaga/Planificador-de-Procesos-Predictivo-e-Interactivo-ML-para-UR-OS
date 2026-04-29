# Git Workflow & Branching Strategy

To keep the team organized and avoid merge conflicts between the Java and Python codebases, this project follows a simplified **Feature Branch Workflow**.

## Main Branches
- `main`: The stable version of the project. Code here must always compile and run without errors.
- `develop`: The active integration branch. All completed features are merged here first before making a final release to `main`.

## Feature Branches
Whenever a team member starts a new task, they must branch off from `develop`. The branch naming convention is:
`feature/phaseX-short-description`

### Recommended Branches for this Project:
1. **`feature/phase1-telemetry`**: Used by the *Telemetry Engineer* to add the O(1) metric gathering in the OS core.
2. **`feature/phase1-user-intent`**: Used by the *UI Engineer* to build the boot prompt menu.
3. **`feature/phase2-ml-training`**: Used by the *Data Scientist* to commit Python scripts (Remember: DO NOT commit large CSVs or `.pkl` models here).
4. **`feature/phase3-ml-scheduler`**: Used by the *Scheduler Architect* to build the predictive Java planner.
5. **`feature/phase4-benchmarking`**: Used to create the testing scenarios and evaluation scripts.

## Pull Requests (PRs)
1. Never push directly to `main` or `develop`.
2. When a feature is complete, open a Pull Request against `develop`.
3. At least one other team member should review the code before merging to ensure it complies with the `.agentrules`.
