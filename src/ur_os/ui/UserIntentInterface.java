package ur_os.ui;

import java.util.Scanner;

/**
 * Boot-time prompt that resolves the global User Intent profile consumed by
 * ML_Scheduler and the workload generator. Required deliverable of Fase 4
 * (TEAM_ROLES.md, role 4: QA & Benchmarking / UI).
 *
 * Two modes:
 *  - Interactive: prints the menu and reads from stdin (used when the user
 *    launches UR_OS without arguments).
 *  - Non-interactive: reads the choice from the first CLI argument so the
 *    benchmark runner can iterate profiles without typing into a Scanner.
 */
public final class UserIntentInterface {

    private UserIntentInterface() {}

    /** Resolves the intent string from args (non-interactive) or stdin. */
    public static String resolve(String[] args) {
        int option = (args != null && args.length > 0) ? parseSafe(args[0]) : promptInteractive();
        return mapOptionToIntent(option);
    }

    private static int promptInteractive() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== UR-OS: Selección de Perfil ===");
        System.out.println("1. Ofimática (Textos, lectura)");
        System.out.println("2. Desarrollo (CPU intensivo)");
        System.out.println("3. Multimedia (Alto I/O, Video)");
        System.out.print("Seleccione la intención del usuario (1-3): ");
        return scanner.nextInt();
    }

    private static int parseSafe(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String mapOptionToIntent(int option) {
        switch (option) {
            case 1: return "Office";
            case 2: return "Development";
            case 3: return "Multimedia";
            default: return "Default";
        }
    }
}
