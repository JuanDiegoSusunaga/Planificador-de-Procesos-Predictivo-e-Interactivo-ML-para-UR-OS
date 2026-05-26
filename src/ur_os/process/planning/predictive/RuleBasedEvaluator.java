package ur_os.process.planning.predictive;

import ur_os.telemetry.ProcessMetrics;
import ur_os.telemetry.UserProfile;

/**
 * Placeholder implementation of ML_ModelEvaluator.
 *
 * Reglas if-else derivadas de la tabla de perfiles en Docs/INFO.md.
 * Esta clase existe para que ML_Scheduler sea testable end-to-end mientras
 * el modelo entrenado de la Fase 2 (Mariana) no esta listo. Una vez exportado
 * el arbol de decision, se sustituye esta clase por la generada (misma interfaz).
 *
 * NOTA: el campo metrics.getCpuBursts() actualmente contiene ciclos totales de
 * CPU ejecutados (ver Process.getTotalCpuCyclesExecuted en commits 7e7afd9/36de596),
 * no ráfagas. La cabecera del CSV conserva el nombre por compatibilidad.
 */
public class RuleBasedEvaluator implements ML_ModelEvaluator {

    // Thresholds calibrated against the simulator's structural parameters
    // (Process.NUM_CPU_CYCLES=3 bursts, MAX_CPU_CYCLES=10 per burst, ~2 I/O
    // blocks per fully-run process). The dataset CSV is stale (CpuBursts=2,
    // IoBlocks=0 for every row, from the pre-rename telemetry) and cannot be
    // used for calibration until Fase 1 regenerates it under the new semantics.
    private static final int CPU_BOUND_THRESHOLD = 10;  // above-median CPU cycles
    private static final int IO_BOUND_THRESHOLD  = 2;   // typical block count

    @Override
    public boolean shouldPrioritize(ProcessMetrics metrics, UserProfile profile) {
        int cpu = metrics.getCpuBursts(); // total CPU cycles executed (post-rename)
        int io  = metrics.getIoBlocks();

        switch (profile) {
            case DEVELOPMENT:
                return cpu >= CPU_BOUND_THRESHOLD && io < IO_BOUND_THRESHOLD;
            case OFFICE:
                return io >= IO_BOUND_THRESHOLD && cpu < CPU_BOUND_THRESHOLD;
            case MULTIMEDIA:
                return cpu >= CPU_BOUND_THRESHOLD / 2 && io >= IO_BOUND_THRESHOLD;
            case DEFAULT:
            default:
                return false;
        }
    }
}
