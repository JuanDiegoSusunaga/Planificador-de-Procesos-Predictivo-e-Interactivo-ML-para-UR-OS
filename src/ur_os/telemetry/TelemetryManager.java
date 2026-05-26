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
                writer.write("ProcessID,UserIntent,CpuCycles,IoBlocks,TurnaroundTime");
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
            int id,
            String intent,
            int cpuCycles,
            int ioBlocks,
            int turnaroundTime
    ) {
        if (writer != null) {
            try {
                writer.write(
                        id + "," +
                        intent + "," +
                        cpuCycles + "," +
                        ioBlocks + "," +
                        turnaroundTime
                );
                writer.newLine();
                writer.flush();

            } catch (IOException e) {
                System.err.println("Error guardando datos del proceso: " + id);
            }
        }
    }
}
