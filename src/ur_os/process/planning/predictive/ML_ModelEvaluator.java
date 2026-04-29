package ur_os.process.planning.predictive;

import ur_os.telemetry.ProcessMetrics;
import ur_os.telemetry.UserProfile;

/**
 * Contract (Interface) for any Machine Learning evaluation logic.
 * This allows the Java Scheduler to be programmed independently from the Python model.
 */
public interface ML_ModelEvaluator {
    
    /**
     * Determines if a process should be prioritized based on its metrics and the user profile.
     * 
     * @param metrics The telemetry data of the process (CPU bursts, I/O blocks).
     * @param profile The current global User Intent profile.
     * @return true if the process should be given priority, false otherwise.
     */
    boolean shouldPrioritize(ProcessMetrics metrics, UserProfile profile);
}
