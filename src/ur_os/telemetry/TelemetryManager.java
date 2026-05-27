package ur_os.telemetry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TelemetryManager {

    private static TelemetryManager instance;
    private BufferedWriter writer;

    private final String CSV_PATH = "ml_training/datasets/telemetry_data.csv";

    private TelemetryManager() {
        try {
            File directory = new File("ml_training/datasets");

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(CSV_PATH);

            boolean appendMode = true;
            boolean isNewFile = !file.exists() || !appendMode;

            writer = new BufferedWriter(new FileWriter(file, appendMode));

            if (isNewFile) {
                writer.write("SimulationRun,ProcessID,UserIntent,ProcessSize,Priority,ArrivalTime,CpuCycles,IoBlocks,WaitingTime,TurnaroundTime");
                writer.newLine();
                writer.flush();
            }

        } catch (IOException e) {
            System.err.println("Error iniciando telemetría: " + e.getMessage());
        }
    }

    public static TelemetryManager getInstance() {
        if (instance == null) {
            instance = new TelemetryManager();
        }
        return instance;
    }

    public void exportProcessData(
            int simulationRun,
            int processId,
            String userIntent,
            int processSize,
            int priority,
            int arrivalTime,
            int cpuCycles,
            int ioBlocks,
            int waitingTime,
            int turnaroundTime
    ) {
        if (writer != null) {
            try {
                writer.write(
                        simulationRun + "," +
                        processId + "," +
                        userIntent + "," +
                        processSize + "," +
                        priority + "," +
                        arrivalTime + "," +
                        cpuCycles + "," +
                        ioBlocks + "," +
                        waitingTime + "," +
                        turnaroundTime
                );
                writer.newLine();
                writer.flush();
                System.out.println("Datos guardados: " + processId + "," + userIntent + "," + cpuCycles + "," + ioBlocks + "," + turnaroundTime);
            } catch (IOException e) {
                System.err.println("Error guardando datos del proceso: " + processId);
            }
        }
    }
}