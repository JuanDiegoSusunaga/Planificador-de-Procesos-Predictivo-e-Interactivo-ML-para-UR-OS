/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package ur_os.process.planning.predictive;

import ur_os.process.planning.Scheduler;
import ur_os.process.Process;
import ur_os.system.OS;
import ur_os.UR_OS;

import java.util.Collections;
import java.util.Comparator;

/**
 * ML_Scheduler - Planificador Predictivo e Interactivo
 * Reordena la cola de procesos según el perfil del usuario.
 * 
 * @author aleja
 */

public class ML_Scheduler extends Scheduler {

    public ML_Scheduler(OS os) {
        super(os);
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (processes.isEmpty() || !cpuEmpty) {
            return;
        }

        Process next = processes.removeFirst();
        System.out.println("[ML_Scheduler] Despachando proceso PID: " + next.getPid() + 
                           " | Perfil: " + UR_OS.globalUserIntent);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        reorderQueueByProfile();
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        reorderQueueByProfile();
    }

    private void reorderQueueByProfile() {
        String intent = UR_OS.globalUserIntent;
        if (intent == null || intent.isEmpty()) {
            intent = "Default";
        }

        switch (intent) {
            case "Development":
                // Priorizamos procesos con más ciclos de CPU ejecutados
                Collections.sort(processes, Comparator.comparingInt(p -> -p.getTotalCpuCyclesExecuted()));
                System.out.println("[ML_Scheduler] Reordenando para DESARROLLO (prioridad CPU)");
                break;

            case "Multimedia":
                // Priorizamos procesos con más bloques de I/O
                Collections.sort(processes, Comparator.comparingInt(p -> -p.getIoBlockCount()));
                System.out.println("[ML_Scheduler] Reordenando para MULTIMEDIA (prioridad I/O)");
                break;

            case "Office":
            default:
                // FCFS - orden por tiempo de llegada
                Collections.sort(processes, Comparator.comparingInt(Process::getArrivalTime));
                System.out.println("[ML_Scheduler] Reordenando para OFICINA (FCFS)");
                break;
        }
    }
}