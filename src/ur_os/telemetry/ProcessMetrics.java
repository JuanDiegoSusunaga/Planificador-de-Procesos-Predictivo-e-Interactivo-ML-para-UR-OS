package ur_os.telemetry;

/**
 * A simple POJO (Data container) to store telemetry data for a single process.
 * This class isolates the raw data from the complex Process simulation logic.
 */
public class ProcessMetrics {
    private final String processId;
    private int cpuBursts;
    private int ioBlocks;

    /**
     * Initializes the metrics for a new process.
     * @param processId The unique identifier of the process.
     */
    public ProcessMetrics(String processId) {
        this.processId = processId;
        this.cpuBursts = 0;
        this.ioBlocks = 0;
    }

    public void incrementCpuBursts() { 
        this.cpuBursts++; 
    }
    
    public void incrementIoBlocks() { 
        this.ioBlocks++; 
    }
    
    public String getProcessId() { 
        return processId; 
    }
    
    public int getCpuBursts() { 
        return cpuBursts; 
    }
    
    public int getIoBlocks() { 
        return ioBlocks; 
    }
}
