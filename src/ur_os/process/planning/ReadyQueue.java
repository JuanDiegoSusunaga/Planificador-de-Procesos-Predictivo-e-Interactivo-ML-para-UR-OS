/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ur_os.process.planning;

import java.util.ArrayList;
import ur_os.process.Process;
import ur_os.process.planning.predictive.ML_Scheduler;
import ur_os.process.planning.predictive.RuleBasedEvaluator;
import ur_os.system.OS;

/**
 *
 * @author super
 */
public class ReadyQueue {
    
    Scheduler s;
    OS os;
    
    
    public ReadyQueue(OS os){
        this.os = os;
        s = buildScheduler(os);
    }

    // Selección por System property -Dur_os.scheduler=FCFS|RR|SJF_NP|SJF_P|MFQ|PRIORITY|ML
    // Default: FCFS. Habilita el benchmark de Fase 4 sin recompilar entre corridas.
    private static Scheduler buildScheduler(OS os){
        String pick = System.getProperty("ur_os.scheduler", "FCFS").toUpperCase();
        switch (pick) {
            case "RR":       return new RoundRobin(os, 6);
            case "SJF_NP":   return new SJF_NP(os);
            case "SJF_P":    return new SJF_P(os);
            case "MFQ":      return new MFQ(os, new RoundRobin(os,3), new RoundRobin(os,6), new FCFS(os));
            case "PRIORITY": return new PriorityQueue(os,
                                new RoundRobin(os,9,false),
                                new RoundRobin(os,6,false),
                                new RoundRobin(os,3,false),
                                new RoundRobin(os,2,false));
            case "ML":       return new ML_Scheduler(os, new RuleBasedEvaluator());
            case "FCFS":
            default:         return new FCFS(os);
        }
    }
    
    public ReadyQueue(OS OS, Scheduler s){
        this.os = os;
        this.s = s;
    }
    
    public void addProcess(Process p){
        s.addProcess(p);
    }
    
    public Process removeProcess(Process p){
        return s.removeProcess(p);
    }
    
    public void update(){
        s.update();
    }
        
    public String toString(){
        
        return s.toString();
    }
    
   
    
}
