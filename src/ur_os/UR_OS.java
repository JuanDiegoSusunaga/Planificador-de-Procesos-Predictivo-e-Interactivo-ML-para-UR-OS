/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ur_os;

import ur_os.system.SystemOS;
import ur_os.system.SimulationType;
import java.util.Scanner

/**
 *
 * @author super
 */
public class UR_OS {

    private static String VERSION = "0.0.5.4";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== UR-OS: Selección de Perfil ===");
        System.out.println("1. Ofimática (Textos, lectura)");
        System.out.println("2. Desarrollo (CPU intensivo)");
        System.out.println("3. Multimedia (Alto I/O, Video)");
        System.out.print("Seleccione la intención del usuario (1-3): ");
        
        int opcion = scanner.nextInt();
        String globalUserIntent = "Unknown";
        
        switch (opcion) {
            case 1: globalUserIntent = "Office"; break;
            case 2: globalUserIntent = "Development"; break;
            case 3: globalUserIntent = "Multimedia"; break;
            default: globalUserIntent = "Default"; break;
        }
        
        System.out.println("************************************");
        System.out.println("         UR_OS V."+VERSION);
        System.out.println("************************************");
        
        
        
        SystemOS system = new SystemOS(SimulationType.MEMORY_MANAGEMENT);
        
        new Thread(system).start();
        
    }
    
}
