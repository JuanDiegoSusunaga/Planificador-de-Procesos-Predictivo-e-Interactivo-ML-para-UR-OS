package ur_os.system;

import ur_os.UR_OS;
import ur_os.memory.MemoryManagerType;
import ur_os.memory.ProcessMemoryManager;
import ur_os.memory.SystemMemoryManager;
import ur_os.memory.contiguous.PMM_Contiguous;
import ur_os.memory.contiguous.SMM_Contiguous;
import ur_os.memory.freememorymagament.BestFitMemorySlotManager;
import ur_os.memory.freememorymagament.FirstFitMemorySlotManager;
import ur_os.memory.freememorymagament.FreeFramesManager;
import ur_os.memory.freememorymagament.FreeMemoryManager;
import ur_os.memory.freememorymagament.FreeMemorySlotManager;
import ur_os.memory.freememorymagament.FreeMemorySlotManagerType;
import ur_os.memory.freememorymagament.MemorySlot;
import ur_os.memory.freememorymagament.WorstFitMemorySlotManager;
import ur_os.memory.paging.MemoryPageExchange;
import ur_os.memory.paging.PMM_Paging;
import ur_os.memory.paging.SMM_Paging;
import ur_os.memory.segmentation.PMM_Segmentation;
import ur_os.memory.segmentation.SMM_Segmentation;
import ur_os.process.Process;
import ur_os.process.ProcessState;
import ur_os.process.planning.ReadyQueue;
import ur_os.telemetry.TelemetryManager;
import ur_os.virtualmemory.PVMM_FIFO;
import ur_os.virtualmemory.PVMM_LFU;
import ur_os.virtualmemory.PVMM_LRU;
import ur_os.virtualmemory.PVMM_MFU;
import ur_os.virtualmemory.ProcessVirtualMemoryManagerType;

import java.util.Random;

public class OS {

    ReadyQueue rq;
    IOQueue ioq;

    private static int process_count = 0;

    SystemOS system;
    CPU cpu;

    SystemMemoryManager smm;

    FreeMemoryManager fmm;
    FreeMemoryManager fvmm;

    Random r;

    boolean lazySwap;

    public static final int MAX_PROCESS_PRIORITY = 10;
    public static final int PAGE_SIZE = 64;

    public static final MemoryManagerType SMM = MemoryManagerType.CONTIGUOUS;
    public static final FreeMemorySlotManagerType MSM = FreeMemorySlotManagerType.FIRST_FIT;
    public static final ProcessVirtualMemoryManagerType PVMM = ProcessVirtualMemoryManagerType.LRU;

    public static final int FRAMES_PER_PROCESS = 3;
    public static final boolean VIRTUAL_MEMORY_MODE_ON = false;

    public OS(SystemOS system, CPU cpu, IOQueue ioq) {
        rq = new ReadyQueue(this);

        this.ioq = ioq;
        this.system = system;
        this.cpu = cpu;

        lazySwap = false;

        if (SMM == MemoryManagerType.PAGING) {
            smm = new SMM_Paging(this);
            fmm = new FreeFramesManager(SystemOS.MEMORY_SIZE);
            fvmm = new FreeFramesManager(SystemOS.SWAP_MEMORY_SIZE);
        } else {
            switch (SMM) {
                case CONTIGUOUS:
                    smm = new SMM_Contiguous(this);
                    break;

                case SEGMENTATION:
                    smm = new SMM_Segmentation(this);
                    break;

                default:
                    smm = new SMM_Contiguous(this);
                    break;
            }

            switch (MSM) {
                case FIRST_FIT:
                    fmm = new FirstFitMemorySlotManager(SystemOS.MEMORY_SIZE);
                    fvmm = new FirstFitMemorySlotManager(SystemOS.SWAP_MEMORY_SIZE);
                    break;

                case BEST_FIT:
                    fmm = new BestFitMemorySlotManager(SystemOS.MEMORY_SIZE);
                    fvmm = new BestFitMemorySlotManager(SystemOS.SWAP_MEMORY_SIZE);
                    break;

                case WORST_FIT:
                    fmm = new WorstFitMemorySlotManager(SystemOS.MEMORY_SIZE);
                    fvmm = new WorstFitMemorySlotManager(SystemOS.SWAP_MEMORY_SIZE);
                    break;

                default:
                    fmm = new FirstFitMemorySlotManager(SystemOS.MEMORY_SIZE);
                    fvmm = new FirstFitMemorySlotManager(SystemOS.SWAP_MEMORY_SIZE);
                    break;
            }
        }

        r = new Random();
    }

    public void update() {
        rq.update();
    }

    public boolean isCPUEmpty() {
        return cpu.isEmpty();
    }

    public Process getProcessInCPU() {
        return cpu.getProcess();
    }

    public void interrupt(InterruptType t, Process p) {
        interrupt(t, p, null);
    }

    public void interrupt(InterruptType t, Process p, Object i) {
        ProcessMemoryManager pmm;
        MemorySlot m;
        MemorySlot vm;
        MemoryPageExchange mpe;

        switch (t) {
            case CPU_TO_MEMORY:
                cpu.addProcessToMemoryUnit(p);
                break;

            case CPU_TO_IO:
                ioq.addProcess(p);
                break;

            case FINISH_PROCESS:
                p.setState(ProcessState.FINISHED);
                p.setTime_finished(system.getTime());

                System.out.println("Process Terminated: " + p.getPid() + " " + p.getSize());

                fmm.reclaimMemory(p);
                system.showFreeMemory();

                int turnaroundTime = system.getTime() - p.getArrivalTime();

                TelemetryManager.getInstance().exportProcessData(
                        p.getPid(),
                        p.getUserIntent(),
                        p.getTotalCpuCyclesExecuted(),
                        p.getIoBlockCount(),
                        turnaroundTime
                );

                break;

            case IO_DONE:
                rq.addProcess(p);
                break;

            case MEMORY_DONE:
                rq.addProcess(p);
                break;

            case SCHEDULER_CPU_TO_RQ:
                Process temp = cpu.extractProcess();
                rq.addProcess(temp);

                if (p != null) {
                    cpu.addProcess(p);
                }

                break;

            case SCHEDULER_RQ_TO_CPU:
                cpu.addProcess(p);
                break;

            case LOAD_SLOT:
                vm = (MemorySlot) i;
                m = getMemorySlot(vm.getSize());

                pmm = p.getPMM();

                if (pmm instanceof PMM_Contiguous) {
                    PMM_Contiguous pmmc = (PMM_Contiguous) pmm;
                    pmmc.setMemorySlot(m);
                    cpu.loadSlot(m, vm);
                    pmmc.setValid(true);
                }

                break;

            case STORE_SLOT:
                pmm = p.getPMM();

                if (pmm instanceof PMM_Contiguous) {
                    PMM_Contiguous pmmc = (PMM_Contiguous) pmm;

                    if (pmmc.isDirty()) {
                        m = pmmc.getMemorySlot();
                        vm = pmmc.getVMemorySlot();

                        cpu.storeSlot(m, vm);
                        fmm.reclaimMemory(p);
                    }

                    pmmc.setValid(false);
                }

                break;

            case LOAD_PAGE:
                mpe = (MemoryPageExchange) i;
                pmm = p.getPMM();

                if (pmm instanceof PMM_Paging) {
                    cpu.loadPage(mpe.getFrameVictim(), mpe.getFrameToLoadFromSwap());
                }

                break;

            case STORE_PAGE:
                mpe = (MemoryPageExchange) i;
                pmm = p.getPMM();

                if (pmm instanceof PMM_Paging) {
                    cpu.storePage(mpe.getFrameVictim(), mpe.getFrameVictimInSwap());
                }

                if (!mpe.isFullExchange()) {
                    FreeFramesManager ffmm = (FreeFramesManager) fmm;
                    ffmm.reclaimFrame(mpe.getFrameVictim());
                }

                break;
        }
    }

    public void removeProcessFromCPU() {
        cpu.removeProcess();
    }

    public void create_process() {
        create_process(null);
    }

    public void create_process(Process p) {
        if (p != null) {
            p.setPid(process_count++);
        } else {
            p = new Process(process_count++, system.getTime());
        }

        p.setArrivalTime(system.getTime());

        if (p.getUserIntent() == null || p.getUserIntent().equals("Unknown")) {
            p.setUserIntent(UR_OS.globalUserIntent);
        }

        rq.addProcess(p);

        ProcessMemoryManager pmm;

        switch (SMM) {
            case PAGING:
                if (p.getSize() == 0) {
                    pmm = new PMM_Paging(p, r.nextInt(SystemOS.MAX_PROC_SIZE - 1) + 1, 0);
                } else {
                    pmm = new PMM_Paging(p, p.getSize(), 0);
                }

                PMM_Paging pmmp = (PMM_Paging) pmm;

                if (VIRTUAL_MEMORY_MODE_ON) {
                    pmmp.setAssignedPages(FRAMES_PER_PROCESS);
                } else {
                    pmmp.setAssignedPages(-1);
                }

                p.setPMM(pmm);
                assignFramesToProcess(p);
                break;

            case SEGMENTATION:
                if (p.getSize() == 0) {
                    pmm = new PMM_Segmentation(r.nextInt(SystemOS.MAX_PROC_SIZE - 1) + 1);
                } else {
                    pmm = new PMM_Segmentation(p.getSize());
                }

                p.setPMM(pmm);
                assignSegmentsToProcess(p);
                break;

            case CONTIGUOUS:
            default:
                if (VIRTUAL_MEMORY_MODE_ON) {
                    if (p.getSize() == 0) {
                        pmm = new PMM_Contiguous(
                                p,
                                getVMemorySlot(r.nextInt(SystemOS.MAX_PROC_SIZE - 1) + 1),
                                this.lazySwap
                        );
                    } else {
                        pmm = new PMM_Contiguous(
                                p,
                                getVMemorySlot(p.getSize()),
                                this.lazySwap
                        );
                    }
                } else {
                    if (p.getSize() == 0) {
                        int tsize = r.nextInt(SystemOS.MAX_PROC_SIZE - 1) + 1;

                        pmm = new PMM_Contiguous(
                                p,
                                getVMemorySlot(tsize),
                                getMemorySlot(tsize),
                                this.lazySwap
                        );
                    } else {
                        pmm = new PMM_Contiguous(
                                p,
                                getVMemorySlot(p.getSize()),
                                getMemorySlot(p.getSize()),
                                this.lazySwap
                        );
                    }
                }

                p.setPMM(pmm);
                break;
        }

        switch (PVMM) {
            case FIFO:
                p.getPMM().setPVMM(new PVMM_FIFO());
                break;

            case LRU:
                p.getPMM().setPVMM(new PVMM_LRU());
                break;

            case LFU:
                p.getPMM().setPVMM(new PVMM_LFU());
                break;

            case MFU:
                p.getPMM().setPVMM(new PVMM_MFU());
                break;
        }
    }

    public MemorySlot getMemorySlot(int size) {
        FreeMemorySlotManager msm = (FreeMemorySlotManager) fmm;
        return msm.getSlot(size);
    }

    public MemorySlot getVMemorySlot(int size) {
        FreeMemorySlotManager vmsm = (FreeMemorySlotManager) fvmm;
        return vmsm.getSlot(size);
    }

    public void assignSegmentsToProcess(Process p) {
        PMM_Segmentation pmm = (PMM_Segmentation) p.getPMM();

        int limit;
        MemorySlot m;
        int ptSize = pmm.getSt().getSize();

        for (int i = 0; i < ptSize; i++) {
            limit = pmm.getSegment(i).getLimit();
            m = this.getMemorySlot(limit);
            pmm.getSegment(i).setMemorySlot(m);
        }
    }

    public int getFreeFrame() {
        FreeFramesManager freeFrames = (FreeFramesManager) fmm;
        return freeFrames.getFrame();
    }

    public MemorySlot getFreeMemorySlot(int size) {
        FreeMemorySlotManager freeSlots = (FreeMemorySlotManager) fmm;
        return freeSlots.getSlot(size);
    }

    public void assignFramesToProcess(Process p) {
        PMM_Paging pmmp = (PMM_Paging) p.getPMM();

        FreeFramesManager vfreeFrames = (FreeFramesManager) fvmm;
        FreeFramesManager freeFrames = (FreeFramesManager) fmm;

        int ptSize = pmmp.getVPT().getSize();

        if (ptSize <= vfreeFrames.getSize()) {
            for (int i = 0; i < ptSize; i++) {
                pmmp.addVFrameID(vfreeFrames.getFrame(), true);

                if (VIRTUAL_MEMORY_MODE_ON) {
                    pmmp.addFrameID(-1);
                } else {
                    pmmp.addFrameID(freeFrames.getFrame(), true);
                }
            }
        } else {
            System.out.println("Error - Process size larger than available memory");
        }

        if (VIRTUAL_MEMORY_MODE_ON) {
            pmmp.setFrameID(0, freeFrames.getFrame());
        }
    }

    public void showProcesses() {
        System.out.println("Process list:");
        System.out.println(rq.toString());
    }

    public SimulationType getSimulationType() {
        return system.getSimulationType();
    }

    public int getClock() {
        return system.getClock();
    }
}
