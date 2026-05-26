package ur_os.process.planning.predictive;

import ur_os.UR_OS;
import ur_os.process.Process;
import ur_os.process.planning.Scheduler;
import ur_os.system.InterruptType;
import ur_os.system.OS;
import ur_os.telemetry.ProcessMetrics;
import ur_os.telemetry.UserProfile;

/**
 * Predictive scheduler driven by the global User Intent profile and an
 * ML_ModelEvaluator (hand-tuned rules or an exported decision tree).
 *
 * Policy: non-preemptive base. In each context switch (getNext, cpuEmpty=true)
 * the queue is scanned and the first process flagged by the evaluator is
 * dispatched to CPU. If no process qualifies, falls back to FCFS.
 *
 * Complexity per scheduling decision: O(n) on the ready queue, with each
 * evaluator call O(1). For typical queue sizes (n &lt; 100) the cost stays
 * sub-millisecond, satisfying the Fase 3 requirement.
 *
 * The evaluator is injected so the same scheduler can run with the placeholder
 * rules (RuleBasedEvaluator) or with the auto-generated rules exported by
 * Fase 2 without code changes here.
 */
public class ML_Scheduler extends Scheduler {

    private final ML_ModelEvaluator evaluator;
    private final UserProfile profile;
    private final OS osRef;

    public ML_Scheduler(OS os, ML_ModelEvaluator evaluator) {
        this(os, evaluator, UserProfile.fromString(UR_OS.globalUserIntent));
    }

    public ML_Scheduler(OS os, ML_ModelEvaluator evaluator, UserProfile profile) {
        super(os);
        this.osRef = os;
        this.evaluator = evaluator;
        this.profile = profile;
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (processes.isEmpty() || !cpuEmpty) return;

        Process chosen = null;
        for (Process p : processes) {
            if (evaluator.shouldPrioritize(snapshot(p), profile)) {
                chosen = p;
                break;
            }
        }
        if (chosen == null) chosen = processes.getFirst();

        processes.remove(chosen);
        osRef.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, chosen);
    }

    // OFFICE-only preemption: per INFO.md, the Office profile favors interactive
    // responsiveness. Processes returning from I/O are precisely the interactive
    // ones (their ioBlocks > 0, the rule that flags them as prioritizable), so
    // we force a re-evaluation by extracting whatever runs on the CPU. The next
    // tick's getNext() will then scan the queue (now including the new arrival)
    // and dispatch the first prioritized one.
    //
    // Development and Multimedia stay non-preemptive to honor "longer quantums"
    // and "resource isolation" respectively.
    @Override
    public void newProcess(boolean cpuEmpty) {
        if (profile == UserProfile.OFFICE && !cpuEmpty) {
            osRef.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
        }
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        if (profile == UserProfile.OFFICE && !cpuEmpty) {
            osRef.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
        }
    }

    public UserProfile getActiveProfile() {
        return profile;
    }

    // Adapts the live Process telemetry to the evaluator's input contract.
    // ProcessMetrics exposes only increment*; we replay the counters once per
    // decision. Values stay small in practice (tens), so cost is negligible.
    private static ProcessMetrics snapshot(Process p) {
        ProcessMetrics m = new ProcessMetrics(String.valueOf(p.getPid()));
        int cpu = p.getTotalCpuCyclesExecuted();
        int io = p.getIoBlockCount();
        for (int i = 0; i < cpu; i++) m.incrementCpuBursts();
        for (int i = 0; i < io; i++) m.incrementIoBlocks();
        return m;
    }

}
