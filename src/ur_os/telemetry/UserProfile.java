package ur_os.telemetry;

/**
 * Defines the global User Intent profiles that dictate the OS scheduling behavior.
 * Using an Enum prevents Magic String errors across the project.
 */
public enum UserProfile {
    DEVELOPMENT,
    OFFICE,
    MULTIMEDIA,
    DEFAULT;

    /**
     * Bridges the String stored in UR_OS.globalUserIntent (set from the boot
     * menu and by the future UserIntentInterface) to the typed enum the
     * scheduler and the ML evaluator consume.
     *
     * Accepts the labels emitted by UR_OS.main ("Office", "Development",
     * "Multimedia") and is case-insensitive. Anything unrecognized returns
     * DEFAULT so callers never get a NullPointerException.
     */
    public static UserProfile fromString(String intent) {
        if (intent == null) return DEFAULT;
        switch (intent.trim().toLowerCase()) {
            case "development": return DEVELOPMENT;
            case "office":      return OFFICE;
            case "multimedia":  return MULTIMEDIA;
            default:            return DEFAULT;
        }
    }
}
