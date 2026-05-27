/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os.process;

public class CPUInstruction extends Instruction{
    
    public CPUInstruction(){
        super(ProcessInstructionType.CPU, 8);  
    }
    
    public CPUInstruction(int cycles){
        super(ProcessInstructionType.CPU, cycles);  
    }
    
    public CPUInstruction(Instruction i){
        this();
        if(i instanceof CPUInstruction){    
            CPUInstruction m = (CPUInstruction)i;
            this.type = m.type;
            this.cycleNumber = m.cycleNumber;
            this.remainingCycles = m.remainingCycles;
        }
    }
}
