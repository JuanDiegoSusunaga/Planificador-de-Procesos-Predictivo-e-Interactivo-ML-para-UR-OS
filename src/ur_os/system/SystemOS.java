package ur_os.system;

import ur_os.memory.Memory;
import ur_os.memory.MemoryInstruction;
import ur_os.memory.MemoryManagerType;
import ur_os.memory.MemoryOperationType;
import ur_os.memory.freememorymagament.FreeMemorySlotManager;
import ur_os.process.EndInstruction;
import ur_os.process.IOInstruction;
import ur_os.process.Instruction;
import ur_os.process.Process;
import ur_os.virtualmemory.SwapMemory;


import java.util.ArrayList;
import java.util.Random;

public class SystemOS implements Runnable {

    SimulationType simType;

    private static int clock = 0;

    private static final int MAX_SIM_CYCLES = Integer.getInteger("ur_os.max_cycles", 1000);
    private static final int MAX_SIM_PROC_CREATION_TIME = 50;
    private static final double PROB_PROC_CREATION = 0.1;

    public static final int MAX_PROC_SIZE = 1000;

    private static Random r = new Random();

    private OS os;
    private CPU cpu;
    private IOQueue ioq;
    private Memory memory;
    private SwapMemory swap;

    public static final int SEED_SEGMENTS = 7401;
    public static final int SEED_PROCESS_SIZE = 9630;

    public static final int MEMORY_SIZE = 1_048_576;
    public static final int SWAP_MEMORY_SIZE = 1_073_741_824;

    protected ArrayList<Process> processes;
    ArrayList<Integer> execution;

    public SystemOS(SimulationType simType) {
        memory = new Memory(MEMORY_SIZE);
        swap = new SwapMemory(MEMORY_SIZE);

        cpu = new CPU(memory, swap);
        ioq = new IOQueue();

        os = new OS(this, cpu, ioq);

        cpu.setOS(os);
        ioq.setOS(os);

        execution = new ArrayList<>();
        processes = new ArrayList<>();

        // -Dur_os.workload=default|stress (Fase 4). Default = 60 procesos por
        // perfil; stress = 200 procesos con bursts mas largos, para evaluar el
        // ML_Scheduler bajo presion sostenida.
        String workload = System.getProperty("ur_os.workload", "default").toLowerCase();
        if (workload.equals("stress")) {
            initSimulationQueueStress();
        } else {
            initSimulationQueueProfileBased();
        }

        showProcesses();

        this.simType = simType;
    }

    public int getTime() {
        return clock;
    }

    public ArrayList<Process> getProcessAtI(int i) {
        ArrayList<Process> ps = new ArrayList<>();

        for (Process process : processes) {
            if (process.getTime_init() == i) {
                ps.add(process);
            }
        }

        return ps;
    }

    public void initSimulationQueue() {
        double tp;
        Process p;

        for (int i = 0; i < MAX_SIM_PROC_CREATION_TIME; i++) {
            tp = r.nextDouble();

            if (PROB_PROC_CREATION >= tp) {
                p = new Process();
                p.setTime_init(clock);
                processes.add(p);
            }

            clock++;
        }

        clock = 0;
    }

    public void initSimulationQueueSimple() {
        Process p;
        int cont = 0;

        for (int i = 0; i < MAX_SIM_PROC_CREATION_TIME; i++) {
            if (i % 4 == 0) {
                p = new Process(cont++, -1, true);
                p.setTime_init(clock);
                processes.add(p);
            }

            clock++;
        }

        clock = 0;
    }

    public void initSimulationQueueProfileBased() {
        processes.clear();

        String intent = ur_os.UR_OS.globalUserIntent;

        if (intent == null || intent.trim().isEmpty()) {
            intent = "Unknown";
        }

        Random rand = new Random();

        int numberOfProcesses = 60;

        for (int i = 0; i < numberOfProcesses; i++) {

            int arrivalTime;

            if (rand.nextDouble() < 0.6) {
                arrivalTime = rand.nextInt(15);
            } else {
                arrivalTime = 15 + rand.nextInt(MAX_SIM_PROC_CREATION_TIME - 15);
            }

            Process p = new Process(i, arrivalTime);

            int tempSize;

            if (intent.equals("Development")) {
                tempSize = 500 + rand.nextInt(500);
            } else if (intent.equals("Multimedia")) {
                tempSize = 300 + rand.nextInt(600);
            } else if (intent.equals("Office")) {
                tempSize = 100 + rand.nextInt(400);
            } else {
                tempSize = 100 + rand.nextInt(MAX_PROC_SIZE - 100);
            }

            p.setSize(tempSize);
            p.setUserIntent(intent);
            p.setArrivalTime(arrivalTime);
            p.setTime_init(arrivalTime);

            int cpu1;
            int cpu2;
            int numberOfIOBlocks;

            double behavior = rand.nextDouble();

            switch (intent) {
                case "Development":
                    if (behavior < 0.70) {
                        cpu1 = 12 + rand.nextInt(25);
                        cpu2 = 12 + rand.nextInt(25);
                        numberOfIOBlocks = rand.nextInt(2);
                    } else if (behavior < 0.90) {
                        cpu1 = 6 + rand.nextInt(15);
                        cpu2 = 6 + rand.nextInt(15);
                        numberOfIOBlocks = 1 + rand.nextInt(3);
                    } else {
                        cpu1 = 3 + rand.nextInt(8);
                        cpu2 = 3 + rand.nextInt(8);
                        numberOfIOBlocks = 2 + rand.nextInt(4);
                    }
                    break;

                case "Multimedia":
                    if (behavior < 0.60) {
                        cpu1 = 4 + rand.nextInt(12);
                        cpu2 = 4 + rand.nextInt(12);
                        numberOfIOBlocks = 3 + rand.nextInt(5);
                    } else if (behavior < 0.90) {
                        cpu1 = 8 + rand.nextInt(15);
                        cpu2 = 8 + rand.nextInt(15);
                        numberOfIOBlocks = 1 + rand.nextInt(3);
                    } else {
                        cpu1 = 15 + rand.nextInt(20);
                        cpu2 = 15 + rand.nextInt(20);
                        numberOfIOBlocks = rand.nextInt(2);
                    }
                    break;

                case "Office":
                    if (behavior < 0.50) {
                        cpu1 = 2 + rand.nextInt(8);
                        cpu2 = 2 + rand.nextInt(8);
                        numberOfIOBlocks = 1 + rand.nextInt(3);
                    } else if (behavior < 0.80) {
                        cpu1 = 1 + rand.nextInt(5);
                        cpu2 = 1 + rand.nextInt(5);
                        numberOfIOBlocks = rand.nextInt(2);
                    } else {
                        cpu1 = 6 + rand.nextInt(12);
                        cpu2 = 6 + rand.nextInt(12);
                        numberOfIOBlocks = 2 + rand.nextInt(3);
                    }
                    break;

                default:
                    cpu1 = 4 + rand.nextInt(12);
                    cpu2 = 4 + rand.nextInt(12);
                    numberOfIOBlocks = rand.nextInt(4);
                    break;
            }

            p.addCPUInstructions(cpu1);

            for (int j = 0; j < numberOfIOBlocks; j++) {
                int ioDuration = 1 + rand.nextInt(10);
                p.addInstruction(new IOInstruction(ioDuration));

                int cpuBetweenIO = 2 + rand.nextInt(8);
                p.addCPUInstructions(cpuBetweenIO);
            }

            p.addCPUInstructions(cpu2);
            p.addInstruction(new EndInstruction());

            processes.add(p);
        }

        clock = 0;
    }

    // Stress workload para Fase 4: misma forma que ProfileBased pero con
    // 200 procesos (3.3x), CPU bursts hasta 2x mas largos y mas IO blocks.
    // Diseñado para saturar el scheduler y exponer diferencias entre algoritmos
    // que con el workload default quedan dentro del ruido estadistico.
    public void initSimulationQueueStress() {
        processes.clear();

        String intent = ur_os.UR_OS.globalUserIntent;
        if (intent == null || intent.trim().isEmpty()) {
            intent = "Unknown";
        }

        Random rand = new Random();
        int numberOfProcesses = 200;

        for (int i = 0; i < numberOfProcesses; i++) {
            int arrivalTime;
            if (rand.nextDouble() < 0.6) {
                arrivalTime = rand.nextInt(30);
            } else {
                arrivalTime = 30 + rand.nextInt(MAX_SIM_PROC_CREATION_TIME * 2 - 30);
            }

            Process p = new Process(i, arrivalTime);
            int tempSize = 200 + rand.nextInt(MAX_PROC_SIZE - 200);
            p.setSize(tempSize);
            p.setUserIntent(intent);
            p.setArrivalTime(arrivalTime);
            p.setTime_init(arrivalTime);

            int cpu1, cpu2, numberOfIOBlocks;
            double behavior = rand.nextDouble();

            switch (intent) {
                case "Development":
                    cpu1 = 20 + rand.nextInt(40);
                    cpu2 = 20 + rand.nextInt(40);
                    numberOfIOBlocks = (behavior < 0.7) ? rand.nextInt(3)
                                                       : 2 + rand.nextInt(4);
                    break;
                case "Multimedia":
                    cpu1 = 10 + rand.nextInt(25);
                    cpu2 = 10 + rand.nextInt(25);
                    numberOfIOBlocks = 4 + rand.nextInt(6);
                    break;
                case "Office":
                    cpu1 = 4 + rand.nextInt(12);
                    cpu2 = 4 + rand.nextInt(12);
                    numberOfIOBlocks = 2 + rand.nextInt(4);
                    break;
                default:
                    cpu1 = 8 + rand.nextInt(20);
                    cpu2 = 8 + rand.nextInt(20);
                    numberOfIOBlocks = 1 + rand.nextInt(4);
                    break;
            }

            p.addCPUInstructions(cpu1);
            for (int j = 0; j < numberOfIOBlocks; j++) {
                int ioDuration = 2 + rand.nextInt(15);
                p.addInstruction(new IOInstruction(ioDuration));
                int cpuBetween = 3 + rand.nextInt(12);
                p.addCPUInstructions(cpuBetween);
            }
            p.addCPUInstructions(cpu2);
            p.addInstruction(new EndInstruction());

            processes.add(p);
        }

        clock = 0;
    }

    public void initSimulationQueueSimpler() {
        int tempSize;

        Process p = new Process(0, 0);
        tempSize = r.nextInt(MAX_PROC_SIZE - 1) + 1;
        p.setSize(tempSize);

        Instruction temp;

        p.addCPUInstructions(3);
        temp = new MemoryInstruction(MemoryOperationType.LOAD, r.nextInt(tempSize), (byte) -1, 4);
        p.addInstruction(temp);
        p.addCPUInstructions(3);
        temp = new EndInstruction();
        p.addInstruction(temp);
        processes.add(p);

        p = new Process(1, 2);
        tempSize = r.nextInt(MAX_PROC_SIZE - 1) + 1;
        p.setSize(tempSize);

        p.addCPUInstructions(3);
        temp = new MemoryInstruction(MemoryOperationType.STORE, r.nextInt(tempSize), (byte) 38, 3);
        p.addInstruction(temp);
        p.addCPUInstructions(3);
        temp = new EndInstruction();
        p.addInstruction(temp);
        processes.add(p);

        p = new Process(2, 6);
        tempSize = r.nextInt(MAX_PROC_SIZE - 1) + 1;
        p.setSize(tempSize);

        p.addCPUInstructions(7);
        temp = new MemoryInstruction(MemoryOperationType.LOAD, r.nextInt(tempSize), (byte) -1, 4);
        p.addInstruction(temp);
        p.addCPUInstructions(5);
        temp = new EndInstruction();
        p.addInstruction(temp);
        processes.add(p);

        p = new Process(3, 8);
        tempSize = r.nextInt(MAX_PROC_SIZE - 1) + 1;
        p.setSize(tempSize);

        p.addCPUInstructions(4);
        temp = new MemoryInstruction(MemoryOperationType.STORE, r.nextInt(tempSize), (byte) 42, 4);
        p.addInstruction(temp);
        p.addCPUInstructions(7);
        temp = new EndInstruction();
        p.addInstruction(temp);
        processes.add(p);

        clock = 0;
    }

    public void initSimulationQueueSimpler3() {
        Process p = new Process(0, 0);
        p.setSize(200);

        Instruction temp;

        p.addCPUInstructions(5);
        temp = new IOInstruction(4);
        p.addInstruction(temp);
        p.addCPUInstructions(3);
        processes.add(p);

        p = new Process(1, 5);
        p.setSize(500);
        p.addCPUInstructions(13);
        temp = new IOInstruction(5);
        p.addInstruction(temp);
        p.addCPUInstructions(16);
        processes.add(p);

        p = new Process(2, 6);
        p.setSize(250);
        p.addCPUInstructions(7);
        temp = new IOInstruction(3);
        p.addInstruction(temp);
        p.addCPUInstructions(5);
        processes.add(p);

        p = new Process(3, 24);
        p.setSize(800);
        p.addCPUInstructions(4);
        temp = new IOInstruction(3);
        p.addInstruction(temp);
        p.addCPUInstructions(7);
        processes.add(p);

        p = new Process(4, 31);
        p.setSize(600);
        p.addCPUInstructions(7);
        temp = new IOInstruction(3);
        p.addInstruction(temp);
        p.addCPUInstructions(7);
        processes.add(p);

        clock = 0;
    }

    public void initSimulationQueueSimpler2() {
        Process p = new Process(false);

        Instruction temp;

        p.addCPUInstructions(15);
        temp = new IOInstruction(12);
        p.addInstruction(temp);
        p.addCPUInstructions(21);
        p.setTime_init(0);
        p.setPid(0);
        processes.add(p);

        p = new Process(false);
        p.addCPUInstructions(8);
        temp = new IOInstruction(4);
        p.addInstruction(temp);
        p.addCPUInstructions(16);
        p.setTime_init(2);
        p.setPid(1);
        processes.add(p);

        p = new Process(false);
        p.addCPUInstructions(10);
        temp = new IOInstruction(15);
        p.addInstruction(temp);
        p.addCPUInstructions(12);
        p.setTime_init(6);
        p.setPid(2);
        processes.add(p);

        p = new Process(false);
        p.addCPUInstructions(9);
        temp = new IOInstruction(6);
        p.addInstruction(temp);
        p.addCPUInstructions(17);
        p.setTime_init(8);
        p.setPid(3);
        processes.add(p);

        clock = 0;
    }

    public boolean isSimulationFinished() {
        boolean finished = true;

        for (Process p : processes) {
            finished = finished && p.isFinished();
        }

        return finished;
    }

    public SimulationType getSimulationType() {
        return simType;
    }

    public int getClock() {
        return clock;
    }

    @Override
    public void run() {
        ArrayList<Process> ps;

        System.out.println("******SIMULATION START******");

        int i = 0;
        Process temp_exec;
        int tempID;

        while (!isSimulationFinished() && i < MAX_SIM_CYCLES) {
            System.out.println("******Clock: " + i + "******");

            if (this.getSimulationType() == SimulationType.ALL ||
                    this.getSimulationType() == SimulationType.PROCESS_PLANNING) {
                System.out.println(cpu);
                System.out.println(ioq);
            }

            ps = getProcessAtI(i);

            for (Process p : ps) {
                os.create_process(p);
                System.out.println("Process Created: " + p.getPid() + "\n" + p);
                showFreeMemory();
            }

            os.update();

            clock++;

            temp_exec = cpu.getProcess();

            if (temp_exec == null) {
                tempID = -1;
            } else {
                tempID = temp_exec.getPid();
            }

            execution.add(tempID);

            cpu.update();
            ioq.update();

            if (this.getSimulationType() == SimulationType.ALL ||
                    this.getSimulationType() == SimulationType.PROCESS_PLANNING) {
                System.out.println("After the cycle: ");
                System.out.println(cpu);
                System.out.println(ioq);
            }

            i++;
        }

        System.out.println("******SIMULATION FINISHES******");

        System.out.println("******Process Execution******");

        for (Integer num : execution) {
            System.out.print(num + " ");
        }

        System.out.println("");

        System.out.println("******Performance Indicators******");
        System.out.println("Total execution cycles: " + clock);
        System.out.println("CPU Utilization: " + this.calcCPUUtilization());
        System.out.println("Throughput: " + this.calcThroughput());
        System.out.println("Average Turnaround Time: " + this.calcTurnaroundTime());
        System.out.println("Average Waiting Time: " + this.calcAvgWaitingTime());
        System.out.println("Average Context Switches: " + this.calcAvgContextSwitches());
        System.out.println("Average Response Time: " + this.calcAvgResponseTime());

        showProcesses();

        memory.showNotNullBytes();
        showFreeMemory();
    }

    public void showFreeMemory() {
        if (OS.SMM == MemoryManagerType.PAGING) {
            System.out.println("Free frame number: " + os.fmm.getSize());
        } else {
            System.out.println("Free Memory Slots (" + os.fmm.getSize() + "): ");
            FreeMemorySlotManager msm = (FreeMemorySlotManager) os.fmm;
            System.out.println(msm);
        }
    }

    public void showProcesses() {
        System.out.println("Process list:");

        StringBuilder sb = new StringBuilder();

        for (Process process : processes) {
            sb.append(process);
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    public double calcCPUUtilization() {
        int cont = 0;

        for (Integer num : execution) {
            if (num == -1) {
                cont++;
            }
        }

        return (execution.size() - cont) / (double) execution.size();
    }

    public double calcTurnaroundTime() {
        double tot = 0;
        int count = 0;

        for (Process p : processes) {
            if (p.getTime_finished() < 0) continue;
            tot += (p.getTime_finished() - p.getTime_init());
            count++;
        }

        return count == 0 ? 0 : tot / count;
    }

    public double calcThroughput() {
        int finished = 0;
        for (Process p : processes) {
            if (p.getTime_finished() >= 0) finished++;
        }
        return (double) finished / execution.size();
    }

    public double calcAvgWaitingTime() {
        double tot = 0;
        int count = 0;

        for (Process p : processes) {
            if (p.getTime_finished() < 0) continue;
            tot += ((p.getTime_finished() - p.getTime_init()) - p.getTotalExecutionTime());
            count++;
        }

        return count == 0 ? 0 : tot / count;
    }

    public double calcAvgContextSwitches() {
        int cont = 1;
        int prev = execution.get(0);

        for (Integer i : execution) {
            if (prev != i) {
                cont++;
                prev = i;
            }
        }

        return cont / (double) processes.size();
    }

    public double calcAvgResponseTime() {
        double tot = 0;
        int count = 0;
        int temp;

        for (Process p : processes) {
            temp = execution.indexOf(p.getPid());
            if (temp < 0) continue;
            tot += (temp - p.getTime_init());
            count++;
        }

        return count == 0 ? 0 : tot / count;
    }
}
