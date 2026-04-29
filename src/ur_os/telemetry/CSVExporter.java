package ur_os.telemetry;

/**
 * Utility class dedicated to exporting telemetry data to a CSV file.
 * Following SOLID principles, this handles File I/O separately from the OS simulation.
 */
public class CSVExporter {
    
    /**
     * Appends the given metrics to the dataset without blocking the OS.
     * 
     * @param metrics The process metrics to save.
     * @param currentProfile The user profile active at the time.
     */
    public static void appendMetrics(ProcessMetrics metrics, UserProfile currentProfile) {
        // TODO: Implement fast, non-blocking file append logic here
    }
}
