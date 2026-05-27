package ur_os.process.planning.predictive;

import ur_os.telemetry.ProcessMetrics;
import ur_os.telemetry.UserProfile;

/**
 * Evaluator backed by the decision tree exported by Fase 2 (Mariana).
 * Source: ml_training/models/scheduler_rules_simple.json (commit 8ab63bf en
 * feature/phase2-ml-training). The 14 leaves are hardcoded as if-else for O(1)
 * inference — no JSON parsing at runtime.
 *
 * IMPORTANT — UserIntentCode mapping: sklearn.LabelEncoder sorts labels
 * alphabetically, so the trained tree expects:
 *   0 = Development
 *   1 = Multimedia
 *   2 = Office
 * The intent_mapping comment in scheduler_rules.json is wrong; trust the code.
 *
 * Behavior summary: the tree mostly prioritizes purely CPU-bound processes
 * (IoBlocks == 0) within specific CpuCycles bands; processes with IO are
 * generally not prioritized. This contrasts with RuleBasedEvaluator (which
 * prioritizes interactive/IO-heavy Office processes), so swapping evaluators
 * yields a meaningful benchmark comparison.
 */
public class TreeBasedEvaluator implements ML_ModelEvaluator {

    @Override
    public boolean shouldPrioritize(ProcessMetrics metrics, UserProfile profile) {
        int code = encodeProfile(profile);
        int cpu = metrics.getCpuBursts();   // total CPU cycles executed
        int io  = metrics.getIoBlocks();

        // Tree from scheduler_rules_simple.json — traversed in JSON order so
        // the first matching condition wins (decisions are mutually exclusive
        // by construction, but the ordering keeps the contract explicit).
        if (io <= 0) {
            if (code <= 0) {                          // Development
                if (cpu <= 45) {
                    return cpu > 36;                  // 37..45 prioritize
                } else {
                    return cpu > 64;                  // >64 prioritize
                }
            } else {                                  // Multimedia or Office
                if (cpu <= 20) return true;
                if (cpu <= 31) return false;
                return true;                          // >31 prioritize
            }
        } else {
            // IoBlocks >= 1 — the tree learned that I/O-touching processes
            // never beat the median TurnaroundTime in Mariana's training set,
            // so all branches return class 0.
            return false;
        }
    }

    private static int encodeProfile(UserProfile profile) {
        switch (profile) {
            case DEVELOPMENT: return 0;
            case MULTIMEDIA:  return 1;
            case OFFICE:      return 2;
            case DEFAULT:
            default:          return 2;               // map unknown to Office
        }
    }
}
